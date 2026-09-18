# Coovery Android TV (WordPress)

Plugin para controlar os **carrosséis da Página inicial** do app **Coovery tv+** nas versões **Android TV** e **celular**.

## Instalação

1. Compacte a pasta `coovery-android-tv` em um ZIP ou copie para `wp-content/plugins/coovery-android-tv/`.
2. Ative **Coovery Android TV** em Plugins no WordPress.
3. Abra **Coovery TV** no menu lateral do wp-admin.

## API

### Android TV

`GET https://coovery.com.br/wp-json/coovery/v1/android-tv/home-carousel`

Banners largos (~2000×626) no topo da home na TV.

### Celular

`GET https://coovery.com.br/wp-json/coovery/v1/android-mobile/home-carousel`

Cards verticais (~1200×1600) no carrossel da home no telefone.

Resposta (exemplo):

```json
{
  "schemaVersion": 2,
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

Instalações antigas com um único conjunto `slides` no wp-admin continuam funcionando: o plugin migra automaticamente para `tvSlides` e inicia `mobileSlides` com os mesmos valores padrão até você configurá-los.

## Links (`linkUrl`)

- Rotas internas: `home`, `live_tv`, `movies`, `series`, `settings`, `search`, `epg`, `downloads`
- Com categoria: `series?categoryId=123`, `live_tv/123`
- URL externa: `https://coovery.com.br/...` (abre no navegador)

O app atualiza cada carrossel automaticamente (cache ~15 minutos) e usa arte local se a API não responder.
