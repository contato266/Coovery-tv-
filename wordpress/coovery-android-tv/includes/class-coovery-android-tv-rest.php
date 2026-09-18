<?php

if (!defined('ABSPATH')) {
    exit;
}

final class Coovery_Android_Tv_Rest {
    public static function register_routes(): void {
        register_rest_route(
            'coovery/v1',
            '/android-tv/home-carousel',
            [
                'methods' => WP_REST_Server::READABLE,
                'callback' => [self::class, 'get_tv_home_carousel'],
                'permission_callback' => '__return_true',
            ]
        );
        register_rest_route(
            'coovery/v1',
            '/android-mobile/home-carousel',
            [
                'methods' => WP_REST_Server::READABLE,
                'callback' => [self::class, 'get_mobile_home_carousel'],
                'permission_callback' => '__return_true',
            ]
        );
    }

    public static function get_tv_home_carousel(WP_REST_Request $request): WP_REST_Response {
        $config = Coovery_Android_Tv_Plugin::get_config();
        return self::build_carousel_response($config['tvSlides']);
    }

    public static function get_mobile_home_carousel(WP_REST_Request $request): WP_REST_Response {
        $config = Coovery_Android_Tv_Plugin::get_config();
        return self::build_carousel_response($config['mobileSlides']);
    }

    private static function build_carousel_response(array $slide_config): WP_REST_Response {
        $slides = [];
        foreach ($slide_config as $index => $slide) {
            if (!is_array($slide)) {
                continue;
            }
            $enabled = array_key_exists('enabled', $slide) ? (bool) $slide['enabled'] : true;
            if (!$enabled) {
                continue;
            }
            $image_url = isset($slide['imageUrl']) ? esc_url_raw(trim((string) $slide['imageUrl'])) : '';
            $link_url = isset($slide['linkUrl']) ? sanitize_text_field(trim((string) $slide['linkUrl'])) : '';
            $id = isset($slide['id']) ? sanitize_key((string) $slide['id']) : 'slide-' . ($index + 1);
            if ($image_url === '') {
                continue;
            }
            $slides[] = [
                'id' => $id,
                'imageUrl' => $image_url,
                'linkUrl' => $link_url,
            ];
        }

        return new WP_REST_Response(
            [
                'schemaVersion' => COOVERY_ANDROID_TV_SCHEMA_VERSION,
                'updatedAt' => gmdate('c'),
                'slides' => $slides,
            ],
            200
        );
    }
}
