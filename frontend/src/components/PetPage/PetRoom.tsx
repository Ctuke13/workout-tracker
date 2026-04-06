import React, {useMemo, useEffect, useCallback, useRef, useState} from 'react';
import {useRive, useStateMachineInput} from '@rive-app/react-canvas';
import {getCurrentSeason, Season, SEASONAL_ASSETS} from '../../types/pet';
import {useAuth} from '../../contexts/AuthContext';
import {usePet} from '../../contexts/PetContext';
import {useSeason} from '../../contexts/SeasonContext';

interface PetRoomProps {
    season?: Season;
}

const STATE_MACHINE_NAME = 'Pet State Machine';

const PetRoom: React.FC<PetRoomProps> = ({season: overrideSeason}) => {
    const {user} = useAuth();
    const {stats, currentAnimation} = usePet();
    const {season: gameSeason} = useSeason();

    // Derive Season type from the game season name (e.g. "Spring 2026" → "spring")
    // Falls back to real calendar season if game season isn't loaded yet
    const getGameSeason = (): Season => {
        if (gameSeason?.seasonName) {
            const name = gameSeason.seasonName.toLowerCase();
            if (name.includes('winter')) return 'winter';
            if (name.includes('spring')) return 'spring';
            if (name.includes('summer')) return 'summer';
            if (name.includes('fall') || name.includes('autumn')) return 'fall';
        }
        return getCurrentSeason();
    };

    const currentSeason = overrideSeason || getGameSeason();
    const seasonalAssets = SEASONAL_ASSETS[currentSeason];
    const prevCleanlinessRef = useRef<number | null>(null);
    const bathingInProgressRef = useRef(false);
    const bathingTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const [bathingComplete, setBathingComplete] = useState(0);


    // ============ RIVE STATE MACHINE ============
    const {rive, RiveComponent} = useRive({
        src: '/assets/pet/rive/baby_wolf_idle.riv',
        artboard: 'Artboard',
        stateMachines: STATE_MACHINE_NAME,
        autoplay: true,
    });

    // State Machine Inputs
    const dirtLevelInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'dirtLevel');
    const triggerFeedInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'triggerFeed');
    const triggerBatheInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'triggerBathe');
    const triggerSleepInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'triggerSleep');
    const triggerWakeInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'triggerWake');
    const triggerCelebrateInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'triggerCelebrate');
    const triggerPlayInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'triggerPlay');
    const isHappyInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'isHappy');
    const isSadInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'isSad');
    const isSleepingInput = useStateMachineInput(rive, STATE_MACHINE_NAME, 'isSleeping');

    // ============ DEBUG LOGGING ============
    useEffect(() => {
        console.log('🎮 Rive instance:', rive);
        console.log('🎮 dirtLevel input:', dirtLevelInput);
        console.log('🎮 triggerFeed input:', triggerFeedInput);
        console.log('🎮 triggerBathe input:', triggerBatheInput);
        console.log('🎮 triggerSleep input:', triggerSleepInput);
        console.log('🎮 triggerWake input:', triggerWakeInput);
        console.log('🎮 triggerCelebrate input:', triggerCelebrateInput);
        console.log('🎮 triggerPlay input:', triggerPlayInput);
        console.log('🎮 isHappy input:', isHappyInput);
        console.log('🎮 isSad input:', isSadInput);
    }, [rive, dirtLevelInput, triggerFeedInput, triggerBatheInput, triggerSleepInput, triggerWakeInput, triggerCelebrateInput, triggerPlayInput, isHappyInput, isSadInput]);

    useEffect(() => {
        if (isSleepingInput && stats) {
            isSleepingInput.value = stats.isSleeping ?? false;
            console.log('🎮 Set isSleeping to:', stats.isSleeping ?? false);
        }
    }, [isSleepingInput, stats]);

    // ============ SMOOTH DIRT ANIMATION ============
    const animateDirtLevel = useCallback((from: number, to: number, durationMs: number) => {
        const startTime = Date.now();

        const tick = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / durationMs, 1);
            const eased = 1 - Math.pow(1 - progress, 2);
            const current = from + (to - from) * eased;

            if (dirtLevelInput) {
                dirtLevelInput.value = current;
            }

            if (progress < 1) {
                requestAnimationFrame(tick);
            }
        };

        requestAnimationFrame(tick);
    }, [dirtLevelInput]);

    // ============ TRACK BATHING INDEPENDENTLY OF currentAnimation ============
    useEffect(() => {
        if (currentAnimation === 'bathe') {
            bathingInProgressRef.current = true;
            if (bathingTimerRef.current) clearTimeout(bathingTimerRef.current);
            bathingTimerRef.current = setTimeout(() => {
                bathingInProgressRef.current = false;
                setBathingComplete(prev => prev + 1); // force dirt effect to re-run with clean stats
            }, 12000);
        }
        return () => {
            if (bathingTimerRef.current) clearTimeout(bathingTimerRef.current);
        };
    }, [currentAnimation]);

    // ============ UPDATE DIRT LEVEL FROM STATS ============
    useEffect(() => {
        if (dirtLevelInput && stats?.cleanliness != null) {
            if (bathingInProgressRef.current) return; // Block all dirt updates during full bathe duration

            const newDirt = Math.round(((100 - stats.cleanliness) / 100) * 60);

            if (prevCleanlinessRef.current === null) {
                dirtLevelInput.value = newDirt;
            } else {
                dirtLevelInput.value = newDirt;
            }

            prevCleanlinessRef.current = stats.cleanliness;
        }
    }, [dirtLevelInput, stats?.cleanliness, currentAnimation, bathingComplete]);

    // ============ RESET MOOD BEFORE ACTIONS ============
    useEffect(() => {
        // When an action animation starts, reset mood to neutral
        // This allows actions to play from any mood state
        if (currentAnimation && isHappyInput && isSadInput) {
            console.log('🔄 Resetting mood to neutral for action:', currentAnimation);
            isHappyInput.value = 0;
            isSadInput.value = 0;
        }
    }, [currentAnimation, isHappyInput, isSadInput]);

    // ============ MOOD-BASED ANIMATION TRIGGERS ============
    useEffect(() => {
        // Only set mood when no action animation is playing
        if (!rive || !stats || currentAnimation) return;

        const stateMachineInputs = rive.stateMachineInputs(STATE_MACHINE_NAME);
        if (!stateMachineInputs) return;

        // Set mood booleans based on stats
        if (stats.mood === 'happy' && isHappyInput && isSadInput) {
            console.log('😊 Pet is HAPPY! Setting isHappy = 1');
            isHappyInput.value = 1;
            isSadInput.value = 0;
        } else if (stats.mood === 'sad' && isHappyInput && isSadInput) {
            console.log('😢 Pet is SAD! Setting isSad = 1');
            isSadInput.value = 1;
            isHappyInput.value = 0;
        } else if (isHappyInput && isSadInput) {
            // Neutral - both false
            console.log('😐 Pet is NEUTRAL (Idle Natural)');
            isHappyInput.value = 0;
            isSadInput.value = 0;
        }

    }, [stats?.mood, rive, currentAnimation, isHappyInput, isSadInput]);

    // ============ FIRE ANIMATION TRIGGERS ============
    useEffect(() => {
        console.log('🎬 currentAnimation changed to:', currentAnimation);
        if (!currentAnimation) return;

        switch (currentAnimation) {
            case 'feed':
                console.log('🎬 Firing triggerFeed:', triggerFeedInput);
                if (triggerFeedInput) triggerFeedInput.fire();
                break;
            case 'bathe':
                console.log('🎬 Firing triggerBathe:', triggerBatheInput);
                if (triggerBatheInput) {
                    triggerBatheInput.fire();
                    const oldDirt = dirtLevelInput?.value ?? 0;
                    setTimeout(() => {
                        animateDirtLevel(oldDirt as number, 0, 2250);
                    }, 7000);
                }
                break;
            case 'sleep':
                console.log('🎬 Firing triggerSleep:', triggerSleepInput);
                if (triggerSleepInput) triggerSleepInput.fire();
                break;
            case 'wake':
                console.log('🎬 Firing triggerWake:', triggerWakeInput);
                if (triggerWakeInput) triggerWakeInput.fire();
                break;
            case 'celebrate':
                console.log('🎬 Firing triggerCelebrate:', triggerCelebrateInput);
                if (triggerCelebrateInput) triggerCelebrateInput.fire();
                break;
            case 'play':
                // Delay to ensure pet is in Idle Natural before triggering Play
                // (mood reset happens first, but we need to wait for Rive to transition)
                console.log('🎬 Waiting for Idle Natural, then firing triggerPlay');
                setTimeout(() => {
                    console.log('🎬 Firing triggerPlay:', triggerPlayInput);
                    if (triggerPlayInput) triggerPlayInput.fire();
                }, 100); // 100ms delay to ensure Idle Natural is active
                break;
        }
    }, [currentAnimation, triggerFeedInput, triggerBatheInput, triggerSleepInput, triggerWakeInput, triggerCelebrateInput, triggerPlayInput, animateDirtLevel, dirtLevelInput]);

    // ============ PARTICLES ============
    const particles = useMemo(() => {
        const count = 12;
        return Array.from({length: count}, (_, i) => ({
            id: i,
            left: Math.random() * 100,
            delay: Math.random() * 5,
            duration: 4 + Math.random() * 3,
            size: 0.4 + Math.random() * 0.4,
        }));
    }, [currentSeason]);

    return (
        <div className="relative w-full aspect-[4/3] rounded-2xl overflow-hidden shadow-xl">

            {/* ============ LAYER 1: Outside Scene (Bottom) ============ */}
            <div
                className="absolute bg-cover bg-center"
                style={{
                    backgroundImage: `url(${seasonalAssets.outsideImage})`,
                    left: '2.5%',
                    top: '5%',
                    width: '30%',
                    height: '60%',
                }}
            />

            {/* ============ LAYER 2: Seasonal Particles (Middle) ============ */}
            <div
                className="absolute overflow-hidden pointer-events-none"
                style={{
                    left: '2.5%',
                    top: '5%',
                    width: '30%',
                    height: '60%',
                }}
            >
                {particles.map((particle) => (
                    <div
                        key={particle.id}
                        className="absolute"
                        style={{
                            left: `${particle.left}%`,
                            top: '-5%',
                            fontSize: `${particle.size}rem`,
                            opacity: 0.9,
                            animation: `windowFall ${particle.duration}s linear infinite`,
                            animationDelay: `${particle.delay}s`,
                        }}
                    >
                        {seasonalAssets.particleEmoji}
                    </div>
                ))}
            </div>

            {/* ============ LAYER 3: Room Interior (Top) ============ */}
            <div
                className="absolute inset-0 bg-cover bg-center"
                style={{backgroundImage: `url(${seasonalAssets.roomImage})`}}
            />

            {/* ============ LAYER 4: Pet (Topmost) ============ */}
            <div className="absolute inset-0 flex items-center justify-center">
                <div className="w-3/4 h-3/4 scale-[1.8] origin-center">
                    <RiveComponent/>
                </div>
            </div>

            {/* Pet Name Tag */}
            {user?.petName && (
                <div
                    className="absolute bottom-4 left-1/2 -translate-x-1/2 bg-white/90 backdrop-blur-sm px-4 py-1.5 rounded-full shadow-lg border-2 border-amber-200">
                    <span className="font-bold text-amber-700">{user.petName}</span>
                </div>
            )}

            {/* Season Badge */}
            <div
                className="absolute top-3 left-3 bg-white/80 backdrop-blur-sm px-3 py-1 rounded-full text-sm font-medium shadow">
                {currentSeason === 'winter' && '❄️ Winter'}
                {currentSeason === 'spring' && '🌸 Spring'}
                {currentSeason === 'summer' && '☀️ Summer'}
                {currentSeason === 'fall' && '🍂 Fall'}
            </div>
        </div>
    );
};

export default PetRoom;