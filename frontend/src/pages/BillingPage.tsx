import React from 'react';
import {
    Container,
    Typography,
    Card,
    CardContent,
    Box,
    Paper,
    Button,
    Chip,
    List,
    ListItem,
    ListItemText,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from '@mui/material';
import {
    CreditCard,
    Receipt,
    Upgrade,
    Check,
    Star,
    Download
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { UserType } from '../types/enums';

const BillingPage: React.FC = () => {
    const { user } = useAuth();

    const subscriptionTiers = [
        {
            name: 'FREE',
            price: '$0',
            period: 'forever',
            current: user?.userType === UserType.REGULAR && !user?.isProfessional,
            features: [
                'Basic workout tracking',
                '30-day history',
                'Exercise library access',
                'Community features'
            ],
            color: 'default'
        },
        {
            name: 'PLUS',
            price: '$9',
            period: 'month',
            current: false, // You'll need to add logic for PLUS tier if it exists
            features: [
                'Everything in Free',
                '12-month history',
                'Workout templates',
                'Advanced analytics',
                'Priority support'
            ],
            color: 'primary',
            popular: true
        },
        {
            name: 'PRO',
            price: '$19',
            period: 'month',
            current: user?.userType === UserType.REGULAR && user?.isProfessional,
            features: [
                'Everything in Plus',
                'Unlimited history',
                'AI progress guidance',
                'Advanced features',
                'Export data'
            ],
            color: 'secondary'
        },
        {
            name: 'PRO PROFESSIONAL',
            price: '$39',
            period: 'month',
            current: user?.userType === UserType.PROFESSIONAL, // Use PROFESSIONAL instead of PRO_PROFESSIONAL
            features: [
                'Everything in Pro',
                'Client management',
                'Custom programs',
                'Business analytics',
                'White-label options'
            ],
            color: 'warning'
        }
    ];

    const billingHistory = [
        {
            date: '2024-12-01',
            description: 'WorkoutTracker Plus - Monthly',
            amount: '$9.00',
            status: 'Paid',
            invoice: 'INV-2024-12-001'
        },
        {
            date: '2024-11-01',
            description: 'WorkoutTracker Plus - Monthly',
            amount: '$9.00',
            status: 'Paid',
            invoice: 'INV-2024-11-001'
        },
        {
            date: '2024-10-01',
            description: 'WorkoutTracker Plus - Monthly',
            amount: '$9.00',
            status: 'Paid',
            invoice: 'INV-2024-10-001'
        }
    ];

    const getCurrentPlanName = (): string => {
        if (!user) return 'FREE';

        if (user.userType === UserType.PROFESSIONAL) return 'PRO PROFESSIONAL';
        if (user.userType === UserType.REGULAR && user.isProfessional) return 'PRO';
        if (user.userType === UserType.ADMIN) return 'ADMIN';

        return 'FREE';
    };

    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
                Billing & Subscription
            </Typography>

            {/* Current Plan */}
            <Paper sx={{ p: 3, mb: 3 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Box>
                        <Typography variant="h6" fontWeight="bold">
                            Current Plan: {getCurrentPlanName()}
                            {user?.userType !== UserType.REGULAR && (
                                <Chip
                                    label="Active"
                                    color="success"
                                    size="small"
                                    sx={{ ml: 2 }}
                                />
                            )}
                        </Typography>
                        <Typography color="text.secondary">
                            {getCurrentPlanName() === 'FREE'
                                ? 'Upgrade anytime to unlock more features'
                                : 'Next billing date: January 1, 2025'
                            }
                        </Typography>
                    </Box>
                    <Button
                        variant="contained"
                        startIcon={<Upgrade />}
                        disabled={user?.userType === UserType.PROFESSIONAL}
                    >
                        {user?.userType === UserType.PROFESSIONAL ? 'Highest Plan' : 'Upgrade'}
                    </Button>
                </Box>
            </Paper>

            {/* Subscription Tiers */}
            <Typography variant="h6" gutterBottom fontWeight="bold">
                Available Plans
            </Typography>

            <Box
                display="grid"
                gridTemplateColumns={{ xs: '1fr', sm: 'repeat(2, 1fr)', lg: 'repeat(4, 1fr)' }}
                gap={2}
                mb={4}
            >
                {subscriptionTiers.map((tier) => (
                    <Card
                        key={tier.name}
                        sx={{
                            position: 'relative',
                            border: tier.current ? 2 : 1,
                            borderColor: tier.current ? 'primary.main' : 'divider',
                            height: '100%'
                        }}
                    >
                        {tier.popular && (
                            <Box
                                sx={{
                                    position: 'absolute',
                                    top: -8,
                                    left: '50%',
                                    transform: 'translateX(-50%)',
                                    bgcolor: 'primary.main',
                                    color: 'white',
                                    px: 2,
                                    py: 0.5,
                                    borderRadius: 1,
                                    fontSize: '0.75rem',
                                    fontWeight: 'bold'
                                }}
                            >
                                <Star sx={{ fontSize: 12, mr: 0.5 }} />
                                POPULAR
                            </Box>
                        )}

                        <CardContent>
                            <Box textAlign="center" mb={2}>
                                <Typography variant="h6" fontWeight="bold">
                                    {tier.name}
                                </Typography>
                                <Box display="flex" alignItems="baseline" justifyContent="center">
                                    <Typography variant="h4" fontWeight="bold" color="primary">
                                        {tier.price}
                                    </Typography>
                                    <Typography color="text.secondary">
                                        /{tier.period}
                                    </Typography>
                                </Box>
                            </Box>

                            <List dense>
                                {tier.features.map((feature, index) => (
                                    <ListItem key={index} sx={{ px: 0 }}>
                                        <Check color="success" sx={{ mr: 1, fontSize: 16 }} />
                                        <ListItemText
                                            primary={feature}
                                            primaryTypographyProps={{ variant: 'body2' }}
                                        />
                                    </ListItem>
                                ))}
                            </List>

                            <Button
                                variant={tier.current ? "outlined" : "contained"}
                                fullWidth
                                sx={{ mt: 2 }}
                                disabled={tier.current}
                            >
                                {tier.current ? 'Current Plan' : `Upgrade to ${tier.name}`}
                            </Button>
                        </CardContent>
                    </Card>
                ))}
            </Box>

            {/* Payment Method and Billing History */}
            <Box display="flex" flexDirection={{ xs: 'column', md: 'row' }} gap={3}>
                {/* Payment Method */}
                <Box flex={1}>
                    <Typography variant="h6" gutterBottom fontWeight="bold">
                        Payment Method
                    </Typography>

                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" mb={2}>
                                <CreditCard color="primary" sx={{ mr: 2 }} />
                                <Typography variant="h6">
                                    •••• •••• •••• 4242
                                </Typography>
                            </Box>
                            <Typography color="text.secondary" gutterBottom>
                                Expires 12/27
                            </Typography>
                            <Button variant="outlined" size="small">
                                Update Payment Method
                            </Button>
                        </CardContent>
                    </Card>
                </Box>

                {/* Billing History */}
                <Box flex={1}>
                    <Typography variant="h6" gutterBottom fontWeight="bold">
                        Billing History
                    </Typography>

                    <TableContainer component={Paper}>
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Date</TableCell>
                                    <TableCell>Amount</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {billingHistory.map((item) => (
                                    <TableRow key={item.invoice}>
                                        <TableCell>{item.date}</TableCell>
                                        <TableCell>{item.amount}</TableCell>
                                        <TableCell>
                                            <Chip
                                                label={item.status}
                                                color="success"
                                                size="small"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <Button
                                                size="small"
                                                startIcon={<Download />}
                                                sx={{ minWidth: 'auto' }}
                                            >
                                                PDF
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Box>
            </Box>

            <Paper sx={{ p: 3, textAlign: 'center', mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                    Questions about billing?
                </Typography>
                <Typography color="text.secondary" gutterBottom>
                    Contact our support team for help with your subscription or billing questions.
                </Typography>
                <Button variant="outlined" startIcon={<Receipt />}>
                    Contact Support
                </Button>
            </Paper>
        </Container>
    );
};

export default BillingPage;