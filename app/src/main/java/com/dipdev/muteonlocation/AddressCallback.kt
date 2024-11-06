package com.dipdev.muteonlocation
// Callback interface to handle address retrieval results
interface AddressCallback {
    fun onSuccess(address: String, latitude: Double, longitude: Double)
    fun onError(errorMessage: String)
}
