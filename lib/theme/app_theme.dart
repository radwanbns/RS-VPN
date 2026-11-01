import 'package:flutter/material.dart';

/// هوية ألوان RS VPN — خلفية داكنة جدًا + سماوي نيون كلون رئيسي
class AppColors {
  static const background = Color(0xFF0A0E17);
  static const surface = Color(0xFF131A27);
  static const surfaceBorder = Color(0xFF1E5F5A);
  static const primary = Color(0xFF17E6E0); // السماوي النيون الرئيسي
  static const primaryDark = Color(0xFF0FB8B3);
  static const textPrimary = Color(0xFFEAF6F6);
  static const textSecondary = Color(0xFF7B8794);
  static const success = Color(0xFF2ED573);
  static const disconnected = Color(0xFF9AA5B1);
}

class AppTheme {
  static ThemeData get dark {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: AppColors.background,
      fontFamily: 'RobotoMono',
      colorScheme: const ColorScheme.dark(
        primary: AppColors.primary,
        surface: AppColors.surface,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.background,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: TextStyle(
          color: AppColors.primary,
          fontWeight: FontWeight.w800,
          fontSize: 24,
          letterSpacing: 0.5,
        ),
      ),
      useMaterial3: true,
    );
  }
}
