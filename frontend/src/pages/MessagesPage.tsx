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
    Badge,
    Paper
} from '@mui/material';
import { Chat, Send, Group } from '@mui/icons-material';

const MessagesPage: React.FC = () => {
    const mockConversations = [
        {
            id: 1,
            name: 'John Fitness',
            avatar: 'JF',
            lastMessage: 'Great workout today! How did your bench press go?',
            time: '2 min ago',
            unread: 2,
            online: true
        },
        {
            id: 2,
            name: 'Fitness Group',
            avatar: 'FG',
            lastMessage: 'Sarah: Just hit a new PR! 🎉',
            time: '1 hour ago',
            unread: 5,
            online: false,
            isGroup: true
        },
        {
            id: 3,
            name: 'Coach Mike',
            avatar: 'CM',
            lastMessage: 'Your form looked great in today\'s video',
            time: '3 hours ago',
            unread: 0,
            online: true
        }
    ];

    return (
        <Container maxWidth="md" sx={{ py: 3 }}>
            <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
                Messages
            </Typography>

            {/* Quick Stats */}
            <Box display="flex" gap={2} mb={3}>
                <Paper sx={{ p: 2, flexGrow: 1, textAlign: 'center' }}>
                    <Chat color="primary" sx={{ fontSize: 24, mb: 1 }} />
                    <Typography variant="body2" color="text.secondary">3 Active Chats</Typography>
                </Paper>
                <Paper sx={{ p: 2, flexGrow: 1, textAlign: 'center' }}>
                    <Send color="success" sx={{ fontSize: 24, mb: 1 }} />
                    <Typography variant="body2" color="text.secondary">7 New Messages</Typography>
                </Paper>
                <Paper sx={{ p: 2, flexGrow: 1, textAlign: 'center' }}>
                    <Group color="warning" sx={{ fontSize: 24, mb: 1 }} />
                    <Typography variant="body2" color="text.secondary">2 Group Chats</Typography>
                </Paper>
            </Box>

            {/* Conversations List */}
            <Card>
                <CardContent sx={{ p: 0 }}>
                    <List>
                        {mockConversations.map((conversation, index) => (
                            <React.Fragment key={conversation.id}>
                                <ListItem
                                    sx={{
                                        cursor: 'pointer',
                                        '&:hover': { bgcolor: 'action.hover' },
                                        py: 2
                                    }}
                                >
                                    <ListItemAvatar>
                                        <Badge
                                            color="success"
                                            variant="dot"
                                            invisible={!conversation.online}
                                            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                                        >
                                            <Avatar
                                                sx={{
                                                    bgcolor: conversation.isGroup ? 'secondary.main' : 'primary.main',
                                                    width: 48,
                                                    height: 48
                                                }}
                                            >
                                                {conversation.avatar}
                                            </Avatar>
                                        </Badge>
                                    </ListItemAvatar>

                                    <ListItemText
                                        primary={
                                            <Box display="flex" justifyContent="space-between" alignItems="center">
                                                <Typography variant="subtitle1" fontWeight="bold">
                                                    {conversation.name}
                                                </Typography>
                                                <Typography variant="caption" color="text.secondary">
                                                    {conversation.time}
                                                </Typography>
                                            </Box>
                                        }
                                        secondary={
                                            <Box display="flex" justifyContent="space-between" alignItems="center">
                                                <Typography
                                                    variant="body2"
                                                    color="text.secondary"
                                                    sx={{
                                                        overflow: 'hidden',
                                                        textOverflow: 'ellipsis',
                                                        whiteSpace: 'nowrap',
                                                        maxWidth: '200px'
                                                    }}
                                                >
                                                    {conversation.lastMessage}
                                                </Typography>
                                                {conversation.unread > 0 && (
                                                    <Badge
                                                        badgeContent={conversation.unread}
                                                        color="primary"
                                                        sx={{ ml: 1 }}
                                                    />
                                                )}
                                            </Box>
                                        }
                                    />
                                </ListItem>
                                {index < mockConversations.length - 1 && <Divider />}
                            </React.Fragment>
                        ))}
                    </List>
                </CardContent>
            </Card>

            <Paper sx={{ p: 3, textAlign: 'center', mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                    Stay Connected
                </Typography>
                <Typography color="text.secondary">
                    Message your workout partners, get coaching tips, and stay motivated with the community.
                </Typography>
            </Paper>
        </Container>
    );
};

export default MessagesPage;