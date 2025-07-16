import React from 'react';
import {
    Container,
    Typography,
    Card,
    CardContent,
    Box,
    Avatar,
    List,
    ListItem,
    ListItemAvatar,
    ListItemText,
    Divider,
    Chip,
    Paper,
    IconButton
} from '@mui/material';
import {
    Notifications,
    EmojiEvents,
    FitnessCenter,
    People,
    MoreVert,
    Circle
} from '@mui/icons-material';

const NotificationsPage: React.FC = () => {
    const mockNotifications = [
        {
            id: 1,
            type: 'achievement',
            title: 'New Personal Record!',
            message: 'You just hit a new PR on Bench Press: 185lbs!',
            time: '5 min ago',
            unread: true,
            icon: <EmojiEvents color="warning" />
        },
        {
            id: 2,
            type: 'workout',
            title: 'Workout Reminder',
            message: 'Upper Body Strength workout scheduled for 3:00 PM',
            time: '1 hour ago',
            unread: true,
            icon: <FitnessCenter color="primary" />
        },
        {
            id: 3,
            type: 'social',
            title: 'Sarah liked your workout',
            message: 'Sarah Johnson liked your workout: "Leg Day Crusher"',
            time: '2 hours ago',
            unread: false,
            icon: <People color="success" />
        },
        {
            id: 4,
            type: 'achievement',
            title: '7-Day Streak!',
            message: 'Congratulations on completing 7 consecutive workout days!',
            time: '1 day ago',
            unread: false,
            icon: <EmojiEvents color="warning" />
        }
    ];

    return (
        <Container maxWidth="md" sx={{ py: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h4" component="h1" fontWeight="bold">
                    Notifications
                </Typography>
                <Chip
                    label={`${mockNotifications.filter(n => n.unread).length} new`}
                    color="primary"
                    size="small"
                />
            </Box>

            {/* Notification Stats */}
            <Box display="flex" gap={2} mb={3}>
                <Paper sx={{ p: 2, flexGrow: 1, textAlign: 'center' }}>
                    <EmojiEvents color="warning" sx={{ fontSize: 24, mb: 1 }} />
                    <Typography variant="body2" color="text.secondary">2 Achievements</Typography>
                </Paper>
                <Paper sx={{ p: 2, flexGrow: 1, textAlign: 'center' }}>
                    <FitnessCenter color="primary" sx={{ fontSize: 24, mb: 1 }} />
                    <Typography variant="body2" color="text.secondary">1 Workout Reminder</Typography>
                </Paper>
                <Paper sx={{ p: 2, flexGrow: 1, textAlign: 'center' }}>
                    <People color="success" sx={{ fontSize: 24, mb: 1 }} />
                    <Typography variant="body2" color="text.secondary">1 Social Update</Typography>
                </Paper>
            </Box>

            {/* Notifications List */}
            <Card>
                <CardContent sx={{ p: 0 }}>
                    <List>
                        {mockNotifications.map((notification, index) => (
                            <React.Fragment key={notification.id}>
                                <ListItem
                                    sx={{
                                        cursor: 'pointer',
                                        '&:hover': { bgcolor: 'action.hover' },
                                        py: 2,
                                        bgcolor: notification.unread ? 'action.hover' : 'transparent'
                                    }}
                                >
                                    <ListItemAvatar>
                                        <Avatar sx={{ bgcolor: 'transparent' }}>
                                            {notification.icon}
                                        </Avatar>
                                    </ListItemAvatar>

                                    <ListItemText
                                        primary={
                                            <Box display="flex" alignItems="center" gap={1}>
                                                {notification.unread && (
                                                    <Circle color="primary" sx={{ fontSize: 8 }} />
                                                )}
                                                <Typography
                                                    variant="subtitle1"
                                                    fontWeight={notification.unread ? 'bold' : 'normal'}
                                                >
                                                    {notification.title}
                                                </Typography>
                                            </Box>
                                        }
                                        secondary={
                                            <Box>
                                                <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                                                    {notification.message}
                                                </Typography>
                                                <Typography variant="caption" color="text.secondary">
                                                    {notification.time}
                                                </Typography>
                                            </Box>
                                        }
                                    />

                                    <IconButton size="small">
                                        <MoreVert />
                                    </IconButton>
                                </ListItem>
                                {index < mockNotifications.length - 1 && <Divider />}
                            </React.Fragment>
                        ))}
                    </List>
                </CardContent>
            </Card>

            <Paper sx={{ p: 3, textAlign: 'center', mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                    Stay Updated
                </Typography>
                <Typography color="text.secondary">
                    Get notified about achievements, workout reminders, and social interactions to stay motivated.
                </Typography>
            </Paper>
        </Container>
    );
};

export default NotificationsPage;