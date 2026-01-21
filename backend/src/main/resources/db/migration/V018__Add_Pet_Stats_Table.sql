-- =============================================================================
-- V018__Add_Pet_Stats_Table.sql
-- Creates pet_stats table for the virtual pet companion system
-- Tracks 4 core stats: Fuel, Motivation, Fatigue, and Cleanliness
-- Implements crystal economy, fatigue/sleep system, neglect tracking, and home selection
-- =============================================================================

-- =====================================================
-- PET_STATS TABLE
-- =====================================================

CREATE TABLE pet_stats (
                           pet_stats_id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL UNIQUE,

    -- ==========================================
    -- CORE STATS (0-100 scale)
    -- ==========================================

    -- Fuel: "Fitness-themed hunger"
    -- Decreases 15/day, restored by spending crystals to buy food
                           fuel INTEGER NOT NULL DEFAULT 100,

    -- Motivation: Long-term consistency stat
    -- Increases with workouts (+15), decreases 5/day without training
    -- GATES BATHING: Must be >= 40 to allow bath
    -- GATES MOTIVATE BUTTON: Need fuel >= 40 to use
                           motivation INTEGER NOT NULL DEFAULT 100,

    -- Fatigue: Exhaustion from training (REPLACES Recovery)
    -- Increases with workouts (+15 per exercise), decreases 20/day
    -- At 100: Forced 24-hour sleep
                           fatigue INTEGER NOT NULL DEFAULT 0,

    -- Cleanliness: Hygiene after workouts
    -- Decreases 5/day + 10/workout, restored by bathing
    -- Tier 1 (60-79%): Deodorant spray +30
    -- Tier 2 (40-59%): Sponge +50
    -- Tier 3 (0-39%): Sponge + showerhead +60
                           cleanliness INTEGER NOT NULL DEFAULT 100,

    -- ==========================================
    -- CRYSTAL ECONOMY
    -- ==========================================

    -- Energy Crystals: Currency earned from workouts
    -- Tiered earning: 1 ex=2, 2 ex=5, 3 ex=7, 5 ex=9, 7+ ex=12
    -- Spent to buy food (Snack=1, Meal=3, Feast=5)
    -- Max capacity: 15 crystals
                           crystals INTEGER NOT NULL DEFAULT 0,

    -- ==========================================
    -- FEEDING EFFICIENCY SYSTEM
    -- ==========================================

    -- Last workout time: Used to calculate feeding efficiency
    -- Today/yesterday: 100% efficiency
    -- 2 days ago: 85%
    -- 3 days ago: 70%
    -- 4 days ago: 55%
    -- 5+ days ago: 40%
                           last_workout_time TIMESTAMP,

    -- Last fed time: Used to track neglect (4+ days without feeding)
                           last_fed_time TIMESTAMP,

    -- ==========================================
    -- FORCED SLEEP SYSTEM
    -- ==========================================

    -- Is sleeping: Pet in forced 24-hour rest state
    -- Triggered when fatigue reaches 100
                           is_sleeping BOOLEAN NOT NULL DEFAULT false,

    -- Sleep start time: When forced sleep began
                           sleep_start_time TIMESTAMP,

    -- Sleep end time: When pet will wake up (24 hours after start)
                           sleep_end_time TIMESTAMP,

    -- ==========================================
    -- NEGLECT SYSTEM
    -- ==========================================

    -- Is neglected: Pet hasn't been fed in 4+ days
    -- Effects: Motivation = 0, all interactions disabled except feed
    -- Recovery: Feed + wait 24 hours
                           is_neglected BOOLEAN NOT NULL DEFAULT false,

    -- Neglect recovery time: When neglect state will end (24h after feeding)
                           neglect_recovery_time TIMESTAMP,

    -- ==========================================
    -- HOME SELECTION
    -- ==========================================

    -- Selected home: Which environment the pet lives in
    -- Options: GYM, NATURE, COZY
    -- Later: BEACH, SPACE, CYBER (premium)
                           selected_home VARCHAR(20) NOT NULL DEFAULT 'GYM',

    -- ==========================================
    -- INTERACTION COOLDOWNS
    -- ==========================================

    -- Motivate: +10 motivation, 12-hour cooldown
    -- Requires: fuel >= 40, !sleeping, !neglected
                           last_motivate_time TIMESTAMP,

    -- Bath: Tier-based restoration, no cooldown
    -- Requires: motivation >= 40, fuel >= 20, !sleeping, !neglected
                           last_bath_time TIMESTAMP,

    -- ==========================================
    -- METADATA
    -- ==========================================

                           last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- ==========================================
    -- CONSTRAINTS
    -- ==========================================

    -- Foreign key to users table
                           CONSTRAINT fk_pet_stats_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(user_id)
                                   ON DELETE CASCADE,

    -- Ensure stats stay within valid ranges
                           CONSTRAINT chk_fuel_range
                               CHECK (fuel >= 0 AND fuel <= 100),

                           CONSTRAINT chk_motivation_range
                               CHECK (motivation >= 0 AND motivation <= 100),

                           CONSTRAINT chk_fatigue_range
                               CHECK (fatigue >= 0 AND fatigue <= 100),

                           CONSTRAINT chk_cleanliness_range
                               CHECK (cleanliness >= 0 AND cleanliness <= 100),

                           CONSTRAINT chk_crystals_range
                               CHECK (crystals >= 0 AND crystals <= 15),

                           CONSTRAINT chk_selected_home
                               CHECK (selected_home IN ('GYM', 'NATURE', 'COZY', 'BEACH', 'SPACE', 'CYBER'))
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Primary lookup by user
CREATE INDEX idx_pet_stats_user_id ON pet_stats(user_id);

-- Query pets by stat levels (for analytics/admin)
CREATE INDEX idx_pet_stats_fuel ON pet_stats(fuel);
CREATE INDEX idx_pet_stats_motivation ON pet_stats(motivation);
CREATE INDEX idx_pet_stats_fatigue ON pet_stats(fatigue);
CREATE INDEX idx_pet_stats_cleanliness ON pet_stats(cleanliness);

-- Query by last update (for scheduled decay jobs)
CREATE INDEX idx_pet_stats_last_updated ON pet_stats(last_updated);

-- Query pets needing attention (low stats / high fatigue)
CREATE INDEX idx_pet_stats_low_fuel ON pet_stats(fuel) WHERE fuel < 30;
CREATE INDEX idx_pet_stats_low_motivation ON pet_stats(motivation) WHERE motivation < 40;
CREATE INDEX idx_pet_stats_high_fatigue ON pet_stats(fatigue) WHERE fatigue >= 70;
CREATE INDEX idx_pet_stats_dirty ON pet_stats(cleanliness) WHERE cleanliness < 40;

-- Query sleeping/neglected pets
CREATE INDEX idx_pet_stats_sleeping ON pet_stats(is_sleeping) WHERE is_sleeping = true;
CREATE INDEX idx_pet_stats_neglected ON pet_stats(is_neglected) WHERE is_neglected = true;

-- Query by last workout (for efficiency calculation)
CREATE INDEX idx_pet_stats_last_workout ON pet_stats(last_workout_time);

-- Query by home type (for analytics)
CREATE INDEX idx_pet_stats_home ON pet_stats(selected_home);

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

COMMENT ON TABLE pet_stats IS 'Tracks all virtual pet statistics, crystal economy, sleep/neglect states, and home selection for the EvoPet companion system';

-- Core Stats
COMMENT ON COLUMN pet_stats.fuel IS 'Energy stat (0-100). Decreases 15/day, restored by spending crystals on food';
COMMENT ON COLUMN pet_stats.motivation IS 'Enthusiasm stat (0-100). Increases with workouts (+15), gates bathing (40+) and motivate button (40+ fuel required)';
COMMENT ON COLUMN pet_stats.fatigue IS 'Exhaustion stat (0-100). Increases per exercise (+15), decreases 20/day. At 100: forced 24h sleep';
COMMENT ON COLUMN pet_stats.cleanliness IS 'Hygiene stat (0-100). Decreases 5/day + 10/workout, restored by 3-tier bathing system';

-- Crystal Economy
COMMENT ON COLUMN pet_stats.crystals IS 'Currency earned from workouts (max 15). Spent on food: Snack=1, Meal=3, Feast=5';
COMMENT ON COLUMN pet_stats.last_workout_time IS 'Last workout timestamp. Used to calculate feeding efficiency (100% if recent, 40% if 5+ days)';
COMMENT ON COLUMN pet_stats.last_fed_time IS 'Last feeding timestamp. Used to detect neglect (4+ days without feeding)';

-- Sleep System
COMMENT ON COLUMN pet_stats.is_sleeping IS 'Pet in forced 24h sleep state (triggered when fatigue reaches 100)';
COMMENT ON COLUMN pet_stats.sleep_start_time IS 'When forced sleep began';
COMMENT ON COLUMN pet_stats.sleep_end_time IS 'When pet will wake up (24h after sleep start)';

-- Neglect System
COMMENT ON COLUMN pet_stats.is_neglected IS 'Pet neglected (4+ days without feeding). Effects: motivation=0, interactions disabled';
COMMENT ON COLUMN pet_stats.neglect_recovery_time IS 'When neglect state ends (24h after feeding during neglect)';

-- Home Selection
COMMENT ON COLUMN pet_stats.selected_home IS 'Environment where pet lives: GYM, NATURE, COZY (or premium: BEACH, SPACE, CYBER)';

-- Interactions
COMMENT ON COLUMN pet_stats.last_motivate_time IS 'Last Motivate interaction (12h cooldown, requires fuel >= 40)';
COMMENT ON COLUMN pet_stats.last_bath_time IS 'Last Bath interaction (no cooldown, requires motivation >= 40 and fuel >= 20)';