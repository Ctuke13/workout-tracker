import React from 'react';
import {
    Container,
    Typography,
    Card,
    CardContent,
    Box,
    Paper,
    Chip,
    IconButton
} from '@mui/material';
import {
    CalendarToday,
    FitnessCenter,
    Schedule,
    Add,
    ChevronLeft,
    ChevronRight
} from '@mui/icons-material';

const CalendarPage: React.FC = () => {
    const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h4" component="h1" fontWeight="bold">
                    Workout Calendar
                </Typography>
                <Box display="flex" alignItems="center">
                    <IconButton>
                        <ChevronLeft />
                    </IconButton>
                    <Typography variant="h6" sx={{ mx: 2 }}>
                        December 2024
                    </Typography>
                    <IconButton>
                        <ChevronRight />
                    </IconButton>
                </Box>
            </Box>

            {/* Week View */}
            <Box
                display="grid"
                gridTemplateColumns="repeat(7, 1fr)"
                gap={2}
                mb={3}
            >
                {daysOfWeek.map((day, index) => (
                    <Paper
                        key={day}
                        sx={{
                            p: 2,
                            minHeight: 200,
                            border: index === 3 ? 2 : 1,
                            borderColor: index === 3 ? 'primary.main' : 'divider',
                            borderStyle: 'dashed'
                        }}
                    >
                        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                            {day}
                        </Typography>
                        <Typography variant="h6" fontWeight="bold" mb={2}>
                            {15 + index}
                        </Typography>

                        {index === 3 && (
                            <Chip
                                label="Upper Body"
                                size="small"
                                color="primary"
                                icon={<FitnessCenter />}
                                sx={{ mb: 1 }}
                            />
                        )}
                        {index === 5 && (
                            <Chip
                                label="Leg Day"
                                size="small"
                                color="success"
                                icon={<FitnessCenter />}
                                sx={{ mb: 1 }}
                            />
                        )}
                    </Paper>
                ))}
            </Box>

            {/* Quick Actions */}
            <Box
                display="grid"
                gridTemplateColumns={{ xs: '1fr', md: 'repeat(3, 1fr)' }}
                gap={2}
                mb={3}
            >
                <Card sx={{ cursor: 'pointer', '&:hover': { boxShadow: 4 } }}>
                    <CardContent>
                        <Box display="flex" alignItems="center">
                            <Add color="primary" sx={{ mr: 2 }} />
                            <Typography variant="h6">Add Exercise</Typography>
                        </Box>
                        <Typography color="text.secondary" variant="body2">
                            Search and add exercises to your calendar
                        </Typography>
                    </CardContent>
                </Card>

                <Card sx={{ cursor: 'pointer', '&:hover': { boxShadow: 4 } }}>
                    <CardContent>
                        <Box display="flex" alignItems="center">
                            <CalendarToday color="success" sx={{ mr: 2 }} />
                            <Typography variant="h6">Workout Plans</Typography>
                        </Box>
                        <Typography color="text.secondary" variant="body2">
                            Add pre-made workout templates
                        </Typography>
                    </CardContent>
                </Card>

                <Card sx={{ cursor: 'pointer', '&:hover': { boxShadow: 4 } }}>
                    <CardContent>
                        <Box display="flex" alignItems="center">
                            <Schedule color="warning" sx={{ mr: 2 }} />
                            <Typography variant="h6">Schedule Program</Typography>
                        </Box>
                        <Typography color="text.secondary" variant="body2">
                            Follow a multi-week program
                        </Typography>
                    </CardContent>
                </Card>
            </Box>

            <Paper sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                    Calendar-Based Workout Planning
                </Typography>
                <Typography color="text.secondary">
                    Drag & drop exercises, schedule workouts, and track your fitness journey with our intuitive calendar interface.
                </Typography>
            </Paper>
        </Container>
    );
};

export default CalendarPage;
