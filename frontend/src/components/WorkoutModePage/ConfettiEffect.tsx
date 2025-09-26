import React, {useEffect, useState, useCallback} from 'react';

interface ConfettiEffectProps {
    show: boolean;
    onComplete: () => void;
    mode?: 'workout-complete' | 'set-complete' | 'personal-record';
}

interface ConfettiPiece {
    id: number;
    x: number;
    y: number;
    vx: number;
    vy: number;
    rotation: number;
    rotationSpeed: number;
    width: number;
    height: number;
    color: string;
    opacity: number;
    drag: number;
    gravity: number;
}

export const ConfettiEffect: React.FC<ConfettiEffectProps> = ({
                                                                  show,
                                                                  onComplete,
                                                                  mode = 'workout-complete'
                                                              }) => {
    const [confetti, setConfetti] = useState<ConfettiPiece[]>([]);

    const createConfetti = useCallback(() => {
        const colors = mode === 'workout-complete'
            ? ['#ff577f', '#ff884b', '#ffd384', '#fff9b0', '#4ecdc4', '#45b7d1']
            : mode === 'personal-record'
                ? ['#ffd700', '#ff6b35', '#f7931e', '#ff8c00', '#ffa500']
                : ['#4ecdc4', '#45b7d1', '#96ceb4'];

        const particleCount = mode === 'personal-record' ? 60 : mode === 'workout-complete' ? 40 : 25;
        const centerX = window.innerWidth / 2;
        const centerY = window.innerHeight * 0.4;

        return Array.from({length: particleCount}, (_, i) => {
            const angle = (Math.PI * 2 * i) / particleCount + Math.random() * 0.5;
            const velocity = Math.random() * 15 + 10;
            const size = Math.random() * 8 + 6;

            return {
                id: i,
                x: centerX + (Math.random() - 0.5) * 100,
                y: centerY + (Math.random() - 0.5) * 100,
                vx: Math.cos(angle) * velocity + (Math.random() - 0.5) * 5,
                vy: Math.sin(angle) * velocity - Math.random() * 5,
                rotation: Math.random() * 360,
                rotationSpeed: (Math.random() - 0.5) * 10,
                width: size,
                height: size * (0.6 + Math.random() * 0.4), // Rectangular confetti
                color: colors[Math.floor(Math.random() * colors.length)],
                opacity: 1,
                drag: 0.98,
                gravity: 0.3 + Math.random() * 0.2
            };
        });
    }, [mode]);

    useEffect(() => {
        if (!show) return;

        const pieces = createConfetti();
        setConfetti(pieces);

        let animationId: number;
        const startTime = Date.now();

        const animate = () => {
            const elapsed = Date.now() - startTime;

            setConfetti(prev => prev.map(piece => {
                // Physics simulation
                const newVx = piece.vx * piece.drag;
                const newVy = piece.vy + piece.gravity;
                const newX = piece.x + newVx;
                const newY = piece.y + newVy;
                const newRotation = piece.rotation + piece.rotationSpeed;

                // Fade out over time
                const fadeStart = 3000;
                const fadeEnd = 5000;
                let newOpacity = piece.opacity;

                if (elapsed > fadeStart) {
                    const fadeProgress = (elapsed - fadeStart) / (fadeEnd - fadeStart);
                    newOpacity = Math.max(0, 1 - fadeProgress);
                }

                return {
                    ...piece,
                    x: newX,
                    y: newY,
                    vx: newVx,
                    vy: newVy,
                    rotation: newRotation,
                    opacity: newOpacity
                };
            }).filter(piece =>
                piece.opacity > 0.01 &&
                piece.y < window.innerHeight + 100 &&
                piece.x > -100 &&
                piece.x < window.innerWidth + 100
            ));

            if (elapsed < 5500) {
                animationId = requestAnimationFrame(animate);
            } else {
                setConfetti([]);
                onComplete();
            }
        };

        animationId = requestAnimationFrame(animate);

        return () => {
            if (animationId) {
                cancelAnimationFrame(animationId);
            }
            setConfetti([]);
        };
    }, [show, onComplete, createConfetti]);

    if (!show) return null;

    return (
        <div className="fixed inset-0 z-50 pointer-events-none overflow-hidden">
            {/* Celebration Message */}
            <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-center">
                    <div className="text-8xl animate-bounce mb-4" style={{animationDuration: '1.5s'}}>
                        {mode === 'workout-complete' ? '🎉' :
                            mode === 'personal-record' ? '🏆' : '✨'}
                    </div>
                    <div className="text-2xl font-bold text-white drop-shadow-2xl">
                        {mode === 'workout-complete' ? 'Workout Complete!' :
                            mode === 'personal-record' ? 'Personal Record!' : 'Great Set!'}
                    </div>
                </div>
            </div>

            {/* Confetti pieces */}
            {confetti.map((piece) => (
                <div
                    key={piece.id}
                    className="absolute"
                    style={{
                        left: `${piece.x}px`,
                        top: `${piece.y}px`,
                        width: `${piece.width}px`,
                        height: `${piece.height}px`,
                        backgroundColor: piece.color,
                        opacity: piece.opacity,
                        transform: `rotate(${piece.rotation}deg)`,
                        borderRadius: '1px',
                        pointerEvents: 'none'
                    }}
                />
            ))}
        </div>
    );
};