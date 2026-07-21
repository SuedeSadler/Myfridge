package com.fridgetracker.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Single shared Supabase client for the app.
 *
 * Fill these in from your Supabase project's Settings > API page.
 * Do NOT commit real values — pull them from local.properties or a
 * build config field instead once you're past prototyping.
 */
object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://nskszjdxxwodkagrakvj.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_nVtxVtfBvz80pX8LFyN3rg_Wq-40ZjR"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Realtime)
        install(Auth)
        install(Storage)
    }
}
