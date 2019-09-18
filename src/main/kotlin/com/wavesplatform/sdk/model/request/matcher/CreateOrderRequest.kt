/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.matcher

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.response.matcher.OrderBookResponse
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWaves
import com.wavesplatform.sdk.utils.isWavesId

/**
 * Create Order Request to DEX-matcher, decentralized exchange of Waves.
 * It collects orders from users who created CreateOrderRequest,
 * matches and sends it to blockchain it by Exchange transactions.
 */
data class CreateOrderRequest(
    /**
     * Matcher Public Key, available in MatcherService.matcherPublicKey() for DEX
     */
    @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
    /**
     * Account public key of the sender in Base58
     */
    @SerializedName("senderPublicKey") var senderPublicKey: String = "",
    /**
     * Exchangeable pair. We sell or buy always amount asset and we always give price asset
     */
    @SerializedName("assetPair") var assetPair: OrderBookResponse.PairResponse = OrderBookResponse.PairResponse(),
    /**
     * Order type "buy" or "sell"
     */
    @SerializedName("orderType") var orderType: String = "buy",
    /**
     * Price for amount
     */
    @SerializedName("price") var price: Long = 0L,
    /**
     * Amount of asset in satoshi
     */
    @SerializedName("amount") var amount: Long = 0L,
    /**
     * Unix time of sending of transaction to blockchain, must be in current time +/- 1.5 hour
     */
    @SerializedName("timestamp") var timestamp: Long = WavesSdk.getEnvironment().getTime(),
    /**
     * Unix time of expiration of transaction to blockchain
     */
    @SerializedName("expiration") var expiration: Long = 0L,
    /**
     * Matcher Fee Asset Id. You can change asset commission
     */
    @SerializedName("matcherFeeAssetId") var matcherFeeAssetId: String = "",
    /**
     * Amount matcher fee of Waves in satoshi
     */
    @SerializedName("matcherFee") var matcherFee: Long = 300000,
    /**
     * Version number of the data structure of the transaction.
     * The value has to be equal to 2
     */
    @SerializedName("version") var version: Byte = WavesConstants.VERSION,
    /**
     * If the array is empty, then S= 3. If the array is not empty,
     * then S = 3 + 2 × N + (P1 + P2 + ... + Pn), where N is the number of proofs in the array,
     * Pn is the size on N-th proof in bytes.
     * The maximum number of proofs in the array is 8. The maximum size of each proof is 64 bytes
     */
    @SerializedName("proofs") var proofs: MutableList<String?>? = null
) {

    private fun toBytes(): ByteArray {
        return if (matcherFeeAssetId.isWaves() || matcherFeeAssetId.isWavesId()) {
            toBytesV2()
        } else {
            toBytesV3()
        }
    }

    private fun toBytesV2(): ByteArray {
        return try {
            val orderTypeByte: Byte = if (orderType == WavesConstants.BUY_ORDER_TYPE) {
                0
            } else {
                1
            }
            Bytes.concat(
                    byteArrayOf(version),
                    WavesCrypto.base58decode(senderPublicKey),
                    WavesCrypto.base58decode(matcherPublicKey),
                    assetPair.toBytes(),
                    byteArrayOf(orderTypeByte),
                    Longs.toByteArray(price),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(expiration),
                    Longs.toByteArray(matcherFee)
            )
        } catch (e: Exception) {
            Log.e("CreateOrderRequest", "Couldn't create toBytes", e)
            ByteArray(0)
        }
    }

    private fun toBytesV3(): ByteArray {
        return try {
            version = 3
            Bytes.concat(
                    toBytesV2(),
                    SignUtil.arrayOption(matcherFeeAssetId))
        } catch (e: Exception) {
            Log.e("CreateOrderRequest", "Couldn't create toBytesV3", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        proofs = mutableListOf(WavesCrypto.base58encode(
            WavesCrypto.signBytesWithPrivateKey(toBytes(), WavesCrypto.base58encode(privateKey))))
    }
}