import React from 'react';
import {
    Container,
    Typography,
    Card,
    CardContent,
    Box,
    Avatar,
    Paper,
    Chip,
    Divider
} from '@mui/material';
import {
    People,
    TrendingUp,
    EmojiEvents,
    FitnessCenter,
    Favorite,
    Comment
} from '@mui/icons-material';

const CommunityPage: React.FC = () => {
    const mockPosts = [
        {
            user: 'Sarah Johnson',
            avatar: 'SJ',
            time: '2 hours ago',
            content: 'Just hit a new PR on deadlifts! 225lbs x 5 💪',
            likes: 24,
            comments: 7,
            achievement: 'New PR'
        },
        {
            user: 'Mike Chen',
            avatar: 'MC',
            time: '4 hours ago',
            content: 'Completed my first full week of the Beginner Strength Program!',
            likes: 18,
            comments: 3,
            achievement: '7-Day Streak'
        }
    ];

    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
                Community
            </Typography>

            {/* Community Stats */}
            <Box
                display="grid"
                gridTemplateColumns={{ xs: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' }}
                gap={2}
                mb={3}
            >
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                    <People color="primary" sx={{ fontSize: 32, mb: 1 }} />
                    <Typography variant="h6" fontWeight="bold">2.3K</Typography>
                    <Typography variant="body2" color="text.secondary">Active Users</Typography>
                </Paper>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                    <TrendingUp color="success" sx={{ fontSize: 32, mb: 1 }} />
                    <Typography variant="h6" fontWeight="bold">847</Typography>
                    <Typography variant="body2" color="text.secondary">New PRs Today</Typography>
                </Paper>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                    <FitnessCenter color="warning" sx={{ fontSize: 32, mb: 1 }} />
                    <Typography variant="h6" fontWeight="bold">1.2K</Typography>
                    <Typography variant="body2" color="text.secondary">Workouts Logged</Typography>
                </Paper>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                    <EmojiEvents color="error" sx={{ fontSize: 32, mb: 1 }} />
                    <Typography variant="h6" fontWeight="bold">156</Typography>
                    <Typography variant="body2" color="text.secondary">Achievements</Typography>
                </Paper>
            </Box>

            {/* Mock Feed */}
            <Typography variant="h6" gutterBottom fontWeight="bold">
                Recent Activity
            </Typography>

            {mockPosts.map((post, index) => (
                <Card key={index} sx={{ mb: 2 }}>
                    <CardContent>
                        <Box display="flex" alignItems="center" mb={2}>
                            <Avatar sx={{ mr: 2, bgcolor: 'primary.main' }}>
                                {post.avatar}
                            </Avatar>
                            <Box flexGrow={1}>
                                <Typography variant="subtitle1" fontWeight="bold">
                                    {post.user}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    {post.time}
                                </Typography>
                            </Box>
                            <Chip
                                label={post.achievement}
                                size="small"
                                color="success"
                                icon={<EmojiEvents />}
                            />
                        </Box>

                        <Typography variant="body1" mb={2}>
                            {post.content}
                        </Typography>

                        <Divider sx={{ mb: 2 }} />

                        <Box display="flex" alignItems="center" gap={3}>
                            <Box display="flex" alignItems="center">
                                <Favorite color="error" sx={{ mr: 0.5, fontSize: 20 }} />
                                <Typography variant="body2">{post.likes}</Typography>
                            </Box>
                            <Box display="flex" alignItems="center">
                                <Comment color="primary" sx={{ mr: 0.5, fontSize: 20 }} />
                                <Typography variant="body2">{post.comments}</Typography>
                            </Box>
                        </Box>
                    </CardContent>
                </Card>
            ))}

            <Paper sx={{ p: 3, textAlign: 'center', mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                    Join the Fitness Community
                </Typography>
                <Typography color="text.secondary">
                    Share your progress, celebrate achievements, and motivate others on their fitness journey.
                </Typography>
            </Paper>
        </Container>
    );
};

export default CommunityPage;