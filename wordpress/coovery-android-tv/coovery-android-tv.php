<?php
/**
 * Plugin Name: Coovery Android TV
 * Description: Controla o carrossel da Página inicial do app Coovery tv+ (Android TV) via REST API.
 * Version: 1.0.0
 * Author: Coovery
 * Text Domain: coovery-android-tv
 */

if (!defined('ABSPATH')) {
    exit;
}

define('COOVERY_ANDROID_TV_OPTION', 'coovery_android_tv_home_carousel_v1');
define('COOVERY_ANDROID_TV_SCHEMA_VERSION', 1);

require_once __DIR__ . '/includes/class-coovery-android-tv-rest.php';
require_once __DIR__ . '/includes/class-coovery-android-tv-admin.php';

final class Coovery_Android_Tv_Plugin {
    public static function init(): void {
        add_action('rest_api_init', [Coovery_Android_Tv_Rest::class, 'register_routes']);
        if (is_admin()) {
            Coovery_Android_Tv_Admin::init();
        }
    }

    public static function default_slides(): array {
        return [
            [
                'id' => 'slide-1',
                'imageUrl' => '',
                'linkUrl' => 'series',
                'enabled' => true,
            ],
            [
                'id' => 'slide-2',
                'imageUrl' => '',
                'linkUrl' => 'live_tv',
                'enabled' => true,
            ],
            [
                'id' => 'slide-3',
                'imageUrl' => '',
                'linkUrl' => 'movies',
                'enabled' => true,
            ],
            [
                'id' => 'slide-4',
                'imageUrl' => '',
                'linkUrl' => 'home',
                'enabled' => true,
            ],
            [
                'id' => 'slide-5',
                'imageUrl' => '',
                'linkUrl' => 'settings',
                'enabled' => true,
            ],
        ];
    }

    public static function get_config(): array {
        $stored = get_option(COOVERY_ANDROID_TV_OPTION);
        if (!is_array($stored)) {
            return [
                'schemaVersion' => COOVERY_ANDROID_TV_SCHEMA_VERSION,
                'slides' => self::default_slides(),
            ];
        }
        if (!isset($stored['slides']) || !is_array($stored['slides'])) {
            $stored['slides'] = self::default_slides();
        }
        $stored['schemaVersion'] = COOVERY_ANDROID_TV_SCHEMA_VERSION;
        return $stored;
    }
}

register_activation_hook(__FILE__, static function (): void {
    if (get_option(COOVERY_ANDROID_TV_OPTION) === false) {
        update_option(COOVERY_ANDROID_TV_OPTION, [
            'schemaVersion' => COOVERY_ANDROID_TV_SCHEMA_VERSION,
            'slides' => Coovery_Android_Tv_Plugin::default_slides(),
        ], false);
    }
});

Coovery_Android_Tv_Plugin::init();
