import React from 'react';
import {
    Container,
    Typography,
    Card,
    CardContent,
    Box,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Paper,
    Button
} from '@mui/material';
import {
    ExpandMore,
    Help,
    Chat,
    Email,
    Phone,
    Article,
    VideoLibrary,
    BugReport
} from '@mui/icons-material';

const HelpPage: React.FC = () => {
    const faqItems = [
        {
            question: 'How do I log my first workout?',
            answer: 'Click the + button in the bottom right corner, select "Log Workout", choose your exercises, and enter your sets, reps, and weights.'
        },
        {
            question: 'What\'s the difference between subscription tiers?',
            answer: 'FREE gives you basic tracking, PLUS adds templates and 12-month history, PRO includes unlimited history and AI features, PRO PROFESSIONAL adds client management.'
        },
        {
            question: 'How do I track my progress?',
            answer: 'Visit the Progress page to see detailed analytics, charts, and trends of your workout performance over time.'
        },
        {
            question: 'Can I use this offline?',
            answer: 'Currently, WorkoutTracker requires an internet connection. We\'re working on offline capabilities for future updates.'
        },
        {
            question: 'How do I cancel my subscription?',
            answer: 'Go to Settings > Billing & Subscription, then click "Manage Subscription" to cancel or modify your plan.'
        }
    ];

    const supportOptions = [
        {
            icon: <Chat color="primary" />,
            title: 'Live Chat',
            description: 'Get instant help from our support team',
            action: 'Start Chat'
        },
        {
            icon: <Email color="success" />,
            title: 'Email Support',
            description: 'Send us a detailed message',
            action: 'Send Email'
        },
        {
            icon: <Phone color="warning" />,
            title: 'Phone Support',
            description: 'Call us during business hours',
            action: 'Call Now'
        }
    ];

    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
                Help & Support
            </Typography>

            <Box display="flex" flexDirection={{ xs: 'column', md: 'row' }} gap={3}>
                {/* FAQ Section */}
                <Box flex={2}>
                    <Typography variant="h6" gutterBottom fontWeight="bold">
                        Frequently Asked Questions
                    </Typography>

                    {faqItems.map((faq, index) => (
                        <Accordion key={index} sx={{ mb: 1 }}>
                            <AccordionSummary expandIcon={<ExpandMore />}>
                                <Typography variant="subtitle1" fontWeight="medium">
                                    {faq.question}
                                </Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Typography color="text.secondary">
                                    {faq.answer}
                                </Typography>
                            </AccordionDetails>
                        </Accordion>
                    ))}

                    {/* Help Resources */}
                    <Paper sx={{ p: 3, mt: 3 }}>
                        <Typography variant="h6" gutterBottom fontWeight="bold">
                            Help Resources
                        </Typography>
                        <List>
                            <ListItem sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
                                <ListItemIcon>
                                    <Article color="primary" />
                                </ListItemIcon>
                                <ListItemText
                                    primary="User Guide"
                                    secondary="Complete guide to using WorkoutTracker"
                                />
                            </ListItem>
                            <ListItem sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
                                <ListItemIcon>
                                    <VideoLibrary color="success" />
                                </ListItemIcon>
                                <ListItemText
                                    primary="Video Tutorials"
                                    secondary="Step-by-step video guides"
                                />
                            </ListItem>
                            <ListItem sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
                                <ListItemIcon>
                                    <BugReport color="warning" />
                                </ListItemIcon>
                                <ListItemText
                                    primary="Report a Bug"
                                    secondary="Help us improve by reporting issues"
                                />
                            </ListItem>
                        </List>
                    </Paper>
                </Box>

                {/* Contact Support Section */}
                <Box flex={1}>
                    <Typography variant="h6" gutterBottom fontWeight="bold">
                        Contact Support
                    </Typography>

                    <Box display="flex" flexDirection="column" gap={2}>
                        {supportOptions.map((option, index) => (
                            <Card key={index} sx={{ cursor: 'pointer', '&:hover': { boxShadow: 4 } }}>
                                <CardContent>
                                    <Box display="flex" alignItems="center" mb={2}>
                                        {option.icon}
                                        <Typography variant="h6" sx={{ ml: 2 }}>
                                            {option.title}
                                        </Typography>
                                    </Box>
                                    <Typography color="text.secondary" variant="body2" mb={2}>
                                        {option.description}
                                    </Typography>
                                    <Button variant="outlined" size="small" fullWidth>
                                        {option.action}
                                    </Button>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>

                    {/* Support Hours */}
                    <Paper sx={{ p: 2, textAlign: 'center', mt: 3 }}>
                        <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                            Support Hours
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Monday - Friday: 9 AM - 6 PM EST<br/>
                            Saturday: 10 AM - 4 PM EST<br/>
                            Sunday: Closed
                        </Typography>
                    </Paper>
                </Box>
            </Box>
        </Container>
    );
};

export default HelpPage;