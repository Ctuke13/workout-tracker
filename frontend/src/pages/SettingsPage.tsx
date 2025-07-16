import React from 'react';
import {
    Container,
    Typography,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Divider,
    Switch,
    Paper,
    Box,
    Avatar,
    Button
} from '@mui/material';
import {
    Person,
    Notifications,
    Security,
    Palette,
    Language,
    Help,
    Logout,
    ChevronRight,
    Edit
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

const SettingsPage: React.FC = () => {
    const { user, logout } = useAuth();

    interface SettingsItem {
        icon: React.ReactElement;
        label: string;
        hasSwitch: boolean;
        checked?: boolean;
        value?: string;
        isAction?: boolean;
    }

    interface SettingsSection {
        title: string;
        items: SettingsItem[];
    }

    const settingsSections: SettingsSection[] = [
        {
            title: 'Account',
            items: [
                { icon: <Person />, label: 'Profile Information', hasSwitch: false },
                { icon: <Edit />, label: 'Edit Profile', hasSwitch: false },
                { icon: <Security />, label: 'Password & Security', hasSwitch: false }
            ]
        },
        {
            title: 'Preferences',
            items: [
                { icon: <Notifications />, label: 'Push Notifications', hasSwitch: true, checked: true },
                { icon: <Palette />, label: 'Dark Mode', hasSwitch: true, checked: false },
                { icon: <Language />, label: 'Language', hasSwitch: false, value: 'English' }
            ]
        },
        {
            title: 'Support',
            items: [
                { icon: <Help />, label: 'Help & Support', hasSwitch: false },
                { icon: <Logout />, label: 'Sign Out', hasSwitch: false, isAction: true }
            ]
        }
    ];

    return (
        <Container maxWidth="md" sx={{ py: 3 }}>
            <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
                Settings & Privacy
            </Typography>

            {/* Profile Header */}
            <Paper sx={{ p: 3, mb: 3 }}>
                <Box display="flex" alignItems="center" gap={3}>
                    <Avatar
                        sx={{
                            width: 64,
                            height: 64,
                            bgcolor: 'primary.main',
                            fontSize: '1.5rem'
                        }}
                    >
                        {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
                    </Avatar>
                    <Box flexGrow={1}>
                        <Typography variant="h6" fontWeight="bold">
                            {user?.firstName} {user?.lastName}
                        </Typography>
                        <Typography color="text.secondary">
                            {user?.email}
                        </Typography>
                        <Typography variant="body2" color="primary" sx={{ mt: 0.5 }}>
                            {user?.userType} Member
                        </Typography>
                    </Box>
                    <Button variant="outlined" startIcon={<Edit />}>
                        Edit
                    </Button>
                </Box>
            </Paper>

            {/* Settings Sections */}
            {settingsSections.map((section, sectionIndex) => (
                <Card key={sectionIndex} sx={{ mb: 2 }}>
                    <CardContent sx={{ p: 0 }}>
                        <Box sx={{ p: 2 }}>
                            <Typography variant="h6" fontWeight="bold" color="primary">
                                {section.title}
                            </Typography>
                        </Box>
                        <Divider />
                        <List sx={{ p: 0 }}>
                            {section.items.map((item, itemIndex) => (
                                <React.Fragment key={itemIndex}>
                                    <ListItem
                                        sx={{
                                            cursor: 'pointer',
                                            '&:hover': { bgcolor: 'action.hover' },
                                            py: 2
                                        }}
                                        onClick={item.label === 'Sign Out' ? logout : undefined}
                                    >
                                        <ListItemIcon>
                                            {item.icon}
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={item.label}
                                            secondary={item.value}
                                        />
                                        {item.hasSwitch ? (
                                            <Switch
                                                checked={item.checked}
                                                color="primary"
                                            />
                                        ) : (
                                            !item.isAction && <ChevronRight color="action" />
                                        )}
                                    </ListItem>
                                    {itemIndex < section.items.length - 1 && <Divider />}
                                </React.Fragment>
                            ))}
                        </List>
                    </CardContent>
                </Card>
            ))}

            <Paper sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                    Privacy & Data
                </Typography>
                <Typography color="text.secondary" variant="body2">
                    Your privacy is important to us. We follow industry best practices to keep your data secure.
                </Typography>
            </Paper>
        </Container>
    );
};

export default SettingsPage;