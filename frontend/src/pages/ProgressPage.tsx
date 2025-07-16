import React from 'react';
import {
    Container,
    Typography,
    Card,
    CardContent,
    Box,
    LinearProgress,
    Paper,
    Avatar
} from '@mui/material';
import { TrendingUp, Timeline, EmojiEvents } from '@mui/icons-material';

const ProgressPage: React.FC = () => {
    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
                Progress Analytics
            </Typography>

            <Box
                display="grid"
                gridTemplateColumns={{ xs: '1fr', md: 'repeat(3, 1fr)' }}
                gap={3}
                mb={3}
            >
                <Card>
                    <CardContent>
                        <Box display="flex" alignItems="center" mb={2}>
                            <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                                <TrendingUp />
                            </Avatar>
                            <Typography variant="h6">Strength Progress</Typography>
                        </Box>
                        <Typography color="text.secondary" gutterBottom>
                            Track your strength gains over time
                        </Typography>
                        <LinearProgress variant="determinate" value={65} sx={{ mt: 1 }} />
                    </CardContent>
                </Card>

                <Card>
                    <CardContent>
                        <Box display="flex" alignItems="center" mb={2}>
                            <Avatar sx={{ bgcolor: 'success.main', mr: 2 }}>
                                <Timeline />
                            </Avatar>
                            <Typography variant="h6">Workout Analytics</Typography>
                        </Box>
                        <Typography color="text.secondary" gutterBottom>
                            Detailed workout performance metrics
                        </Typography>
                        <LinearProgress variant="determinate" value={45} color="success" sx={{ mt: 1 }} />
                    </CardContent>
                </Card>

                <Card>
                    <CardContent>
                        <Box display="flex" alignItems="center" mb={2}>
                            <Avatar sx={{ bgcolor: 'warning.main', mr: 2 }}>
                                <EmojiEvents />
                            </Avatar>
                            <Typography variant="h6">Achievements</Typography>
                        </Box>
                        <Typography color="text.secondary" gutterBottom>
                            Personal records and milestones
                        </Typography>
                        <LinearProgress variant="determinate" value={80} color="warning" sx={{ mt: 1 }} />
                    </CardContent>
                </Card>
            </Box>

            <Paper sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                    Advanced Analytics Coming Soon
                </Typography>
                <Typography color="text.secondary">
                    We're building comprehensive progress tracking with detailed charts, trends, and insights.
                </Typography>
            </Paper>
        </Container>
    );
};

export default ProgressPage;