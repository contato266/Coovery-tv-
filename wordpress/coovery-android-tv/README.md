# Coovery Android TV (WordPress)

Plugin para controlar o **primeiro carrossel** da Página inicial do app **Coovery tv+** na versão Android TV.

## Instalação

1. Compacte a pasta `coovery-android-tv` em um ZIP ou copie para `wp-content/plugins/coovery-android-tv/`.
2. Ative **Coovery Android TV** em Plugins no WordPress.
3. Abra **Coovery TV** no menu lateral do wp-admin.

## API

`GET https://coovery.com.br/wp-json/coovery/v1/android-tv/home-carousel`

Resposta (exemplo):

```json
{
  "schemaVersion": 1,
  "updatedAt": "2026-09-18T12:00:00+00:00",
  "slides": [
    {
      "id": "slide-1",
      "imageUrl": "https://coovery.com.br/wp-content/uploads/banner-1.jpg",
      "linkUrl": "series"
    }
  ]
}
```

Somente slides **ativos** com `imageUrl` **HTTPS** são publicados.

## Links (`linkUrl`)

- Rotas internas: `home`, `live_tv`, `movies`, `series`, `settings`, `search`, `epg`, `downloads`
- Com categoria: `series?categoryId=123`, `live_tv/123`
- URL externa: `https://coovery.com.br/...` (abre no navegador da TV)

O app atualiza o carrossel automaticamente (cache ~15 minutos) e usa os banners locais se a API não responder.
