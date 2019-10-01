package dym.unique.fastcamera.bean

data class PreSettings(var zoom: Int? = null, var flash: Boolean? = null) {
    fun use(): Settings {
        try {
            return Settings(zoom, flash)
        } finally {
            zoom = null
            flash = null
        }
    }

    data class Settings(var zoom: Int?, var flash: Boolean?)
}