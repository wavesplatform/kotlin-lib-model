/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.arrayWithSize
import java.nio.charset.Charset

/**
 * The Alias transaction creates short readable alias for address
 */
class AliasTransaction(
        /**
         * Alias, short name for address in Waves blockchain.
         * Alias bytes must be in [4;30]
         * Alphabet: -.0123456789@_abcdefghijklmnopqrstuvwxyz
         */
        @SerializedName("alias") var alias: String = "")
    : BaseTransaction(CREATE_ALIAS) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(type),
                    byteArrayOf(version),
                    WavesCrypto.base58decode(senderPublicKey),
                    Bytes.concat(
                            byteArrayOf(version),
                            byteArrayOf(chainId),
                            alias.toByteArray(
                                    Charset.forName("UTF-8")).arrayWithSize()
                    ).arrayWithSize(),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Alias Transaction", e)
            ByteArray(0)
        }
    }
}