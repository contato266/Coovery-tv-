<?php

if (!defined('ABSPATH')) {
    exit;
}

final class Coovery_Android_Tv_Admin {
    public static function init(): void {
        add_action('admin_menu', [self::class, 'register_menu']);
        add_action('admin_init', [self::class, 'register_settings']);
    }

    public static function register_menu(): void {
        add_menu_page(
            'Coovery Android TV',
            'Coovery TV',
            'manage_options',
            'coovery-android-tv',
            [self::class, 'render_page'],
            'dashicons-smartphone',
            58
        );
    }

    public static function register_settings(): void {
        register_setting(
            'coovery_android_tv_group',
            COOVERY_ANDROID_TV_OPTION,
            [
                'type' => 'array',
                'sanitize_callback' => [self::class, 'sanitize_config'],
                'default' => Coovery_Android_Tv_Plugin::default_config(),
            ]
        );
    }

    public static function sanitize_config($input): array {
        if (!is_array($input)) {
            return Coovery_Android_Tv_Plugin::get_config();
        }

        $tv_slides = self::sanitize_slides($input['tvSlides'] ?? null);
        $mobile_slides = self::sanitize_slides($input['mobileSlides'] ?? null);

        return [
            'schemaVersion' => COOVERY_ANDROID_TV_SCHEMA_VERSION,
            'tvSlides' => $tv_slides,
            'mobileSlides' => $mobile_slides,
        ];
    }

    private static function sanitize_slides($raw): array {
        if (!is_array($raw)) {
            return Coovery_Android_Tv_Plugin::default_slides();
        }

        $slides = [];
        foreach ($raw as $index => $slide) {
            if (!is_array($slide)) {
                continue;
            }
            $slides[] = [
                'id' => sanitize_key($slide['id'] ?? ('slide-' . ($index + 1))),
                'imageUrl' => esc_url_raw(trim((string) ($slide['imageUrl'] ?? ''))),
                'linkUrl' => sanitize_text_field(trim((string) ($slide['linkUrl'] ?? ''))),
                'enabled' => !empty($slide['enabled']),
            ];
        }

        if ($slides === []) {
            return Coovery_Android_Tv_Plugin::default_slides();
        }

        return $slides;
    }

    public static function render_page(): void {
        if (!current_user_can('manage_options')) {
            return;
        }

        $config = Coovery_Android_Tv_Plugin::get_config();
        $tv_endpoint = rest_url('coovery/v1/android-tv/home-carousel');
        $mobile_endpoint = rest_url('coovery/v1/android-mobile/home-carousel');
        ?>
        <div class="wrap">
            <h1>Coovery tv+ — Carrosséis do app</h1>
            <p>Configure imagens e links do carrossel da <strong>Página inicial</strong> separadamente para <strong>Android TV</strong> e <strong>celular</strong>.</p>
            <p><strong>Android TV:</strong> <code><?php echo esc_html($tv_endpoint); ?></code></p>
            <p><strong>Celular:</strong> <code><?php echo esc_html($mobile_endpoint); ?></code></p>
            <p>Links internos: <code>home</code>, <code>live_tv</code>, <code>movies</code>, <code>series</code>, <code>settings</code>, <code>search</code>, <code>epg</code>, <code>downloads</code>, rotas como <code>series?categoryId=123</code>, ou URLs <code>https://</code>.</p>
            <form method="post" action="options.php">
                <?php settings_fields('coovery_android_tv_group'); ?>
                <h2 style="margin-top:24px;">Carrossel Android TV</h2>
                <p class="description">Banner largo (proporção ~2000×626) no topo da home na TV.</p>
                <?php self::render_slides_table('tvSlides', $config['tvSlides']); ?>

                <h2 style="margin-top:32px;">Carrossel celular</h2>
                <p class="description">Cards verticais (proporção ~1200×1600) no carrossel da home no telefone.</p>
                <?php self::render_slides_table('mobileSlides', $config['mobileSlides']); ?>

                <?php submit_button('Salvar carrosséis'); ?>
            </form>
        </div>
        <?php
    }

    private static function render_slides_table(string $field_key, array $slides): void {
        ?>
        <table class="widefat striped" style="max-width:960px;margin-top:12px;">
            <thead>
                <tr>
                    <th>Ativo</th>
                    <th>ID</th>
                    <th>URL da imagem</th>
                    <th>Link (rota ou URL)</th>
                </tr>
            </thead>
            <tbody>
            <?php foreach ($slides as $index => $slide) : ?>
                <tr>
                    <td>
                        <input type="checkbox" name="<?php echo esc_attr(COOVERY_ANDROID_TV_OPTION); ?>[<?php echo esc_attr($field_key); ?>][<?php echo (int) $index; ?>][enabled]" value="1" <?php checked(!empty($slide['enabled'])); ?> />
                    </td>
                    <td>
                        <input type="text" class="regular-text" name="<?php echo esc_attr(COOVERY_ANDROID_TV_OPTION); ?>[<?php echo esc_attr($field_key); ?>][<?php echo (int) $index; ?>][id]" value="<?php echo esc_attr($slide['id'] ?? ('slide-' . ($index + 1))); ?>" />
                    </td>
                    <td>
                        <input type="url" class="large-text" name="<?php echo esc_attr(COOVERY_ANDROID_TV_OPTION); ?>[<?php echo esc_attr($field_key); ?>][<?php echo (int) $index; ?>][imageUrl]" value="<?php echo esc_attr($slide['imageUrl'] ?? ''); ?>" placeholder="https://coovery.com.br/wp-content/uploads/..." />
                    </td>
                    <td>
                        <input type="text" class="regular-text" name="<?php echo esc_attr(COOVERY_ANDROID_TV_OPTION); ?>[<?php echo esc_attr($field_key); ?>][<?php echo (int) $index; ?>][linkUrl]" value="<?php echo esc_attr($slide['linkUrl'] ?? ''); ?>" placeholder="series" />
                    </td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        <?php
    }
}
