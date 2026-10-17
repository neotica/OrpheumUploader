package id.neotica.orpheum.uploader.utils

import id.neotica.orpheum.uploader.utils.Constants.BASE_URL
import id.neotica.orpheum.uploader.utils.Constants.BASE_URL_BUCKET_PUBLIC

fun resolveMediaUrl(path: String?): String? = when {
    path.isNullOrBlank() -> null
    path.startsWith("http://") || path.startsWith("https://") -> path
    path.startsWith("/media/") -> "$BASE_URL/orpheum$path"
    else -> "$BASE_URL_BUCKET_PUBLIC/$path"
}
