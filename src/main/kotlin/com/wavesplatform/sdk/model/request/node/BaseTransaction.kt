package com.wavesplatform.sdk.model.request.node

import android.os.Parcel
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.keeper.interfaces.BaseTransactionParcelable
import com.wavesplatform.sdk.utils.WavesConstants

/**
 * Base transaction
 */
open class BaseTransaction(
        /**
         * ID of the transaction type. Correct values in [1; 16] @see[BaseTransaction.Companion]
         */
        @SerializedName("type") val type: Byte,

        /**
         * Account public key of the sender in Base58
         */
        @SerializedName("senderPublicKey")
        var senderPublicKey: String = "",

        /**
         * Unix time of sending of transaction to blockchain, must be in current time +/- 1.5 hour
         */
        @SerializedName("timestamp")
        var timestamp: Long = 0L,

        /**
         * A transaction fee is a fee that an account owner pays to send a transaction.
         * Transaction fee in WAVELET
         * [Wiki about Fee](https://docs.wavesplatform.com/en/blockchain/transaction-fee.html)
         */
        @SerializedName("fee")
        var fee: Long = 0L,

        /**
         * Version number of the data structure of the transaction.
         * The value has to be equal to 1, 2 or 3
         */
        @SerializedName("version")
        var version: Byte = WavesConstants.VERSION,

        /**
         * Signatures v2 string set.
         * A transaction signature is a digital signature
         * with which the sender confirms the ownership of the outgoing transaction.
         * If the array is empty, then S= 3. If the array is not empty,
         * then S = 3 + 2 × N + (P1 + P2 + ... + Pn), where N is the number of proofs in the array,
         * Pn is the size on N-th proof in bytes.
         * The maximum number of proofs in the array is 8. The maximum size of each proof is 64 bytes
         */
        @SerializedName("proofs")
        val proofs: MutableList<String> = mutableListOf(),

        /**
         * Signature v1. See also [proofs]
         */
        @SerializedName("signature")
        var signature: String? = null,

        /**
         * Determines the network where the transaction will be published to.
         * T or 84 in bytes for test network,
         * W or 87 in for main network
         */
        @SerializedName("chainId")
        var chainId: Byte = WavesSdk.getEnvironment().chainId) : BaseTransactionParcelable {

    override fun writeBaseToParcel(parcel: Parcel) {
        parcel.apply {
            writeString(senderPublicKey)
            writeLong(timestamp)
            writeLong(fee)
            writeByte(version)
            writeStringList(proofs)
            writeString(signature)
            writeByte(chainId)
        }
    }

    override fun readBaseFromParcel(parcel: Parcel) {
        senderPublicKey = parcel.readString() ?: ""
        timestamp = parcel.readLong()
        fee = parcel.readLong()
        version = parcel.readByte()
        parcel.readStringList(proofs)
        signature = parcel.readString() ?: ""
        chainId = parcel.readByte()
    }

    /**
     * Gets bytes array to sign of the transaction
     */
    open fun toBytes(): ByteArray = byteArrayOf()

    /**
     * Sign the transaction with seed-phrase with current time if null
     * and [WavesConstants.WAVES_MIN_FEE] if it equals 0
     * @param seed Seed-phrase
     */
    open fun sign(seed: String): String {
        if (senderPublicKey == "") {
            senderPublicKey = WavesCrypto.publicKey(seed)
        }
        if (timestamp == 0L) {
            timestamp = WavesSdk.getEnvironment().getTime()
        }
        if (fee == 0L) {
            fee = WavesConstants.WAVES_MIN_FEE
        }
        proofs.clear()
        val signature = getSignedStringWithSeed(seed)
        proofs.add(signature)
        return signature
    }

    fun getSignedBytesWithSeed(seed: String): ByteArray {
        return WavesCrypto.signBytesWithSeed(toBytes(), seed)
    }

    fun getSignedStringWithSeed(seed: String): String {
        return WavesCrypto.base58encode(getSignedBytesWithSeed(seed))
    }

    fun getSignedBytesWithPrivateKey(privateKey: String): ByteArray {
        return WavesCrypto.signBytesWithPrivateKey(toBytes(), privateKey)
    }

    fun getSignedStringWithPrivateKey(privateKey: String): String {
        return WavesCrypto.base58encode(getSignedBytesWithPrivateKey(privateKey))
    }

    companion object {

        const val GENESIS: Byte = 1 // Not using
        const val PAYMENT: Byte = 2 // Not using
        const val ISSUE: Byte = 3
        const val TRANSFER: Byte = 4
        const val REISSUE: Byte = 5
        const val BURN: Byte = 6
        const val EXCHANGE: Byte = 7
        const val CREATE_LEASING: Byte = 8
        const val CANCEL_LEASING: Byte = 9
        const val CREATE_ALIAS: Byte = 10
        const val MASS_TRANSFER: Byte = 11
        const val DATA: Byte = 12
        const val ADDRESS_SCRIPT: Byte = 13
        const val SPONSORSHIP: Byte = 14
        const val ASSET_SCRIPT: Byte = 15
        const val SCRIPT_INVOCATION: Byte = 16

        const val SET_SCRIPT_LANG_VERSION: Byte = 1

        fun getNameBy(type: Byte): String {
            return when (type) {
                GENESIS -> "Genesis"
                PAYMENT -> "Payment"
                ISSUE -> "Issue"
                TRANSFER -> "Transfer"
                REISSUE -> "Reissue"
                BURN -> "Burn"
                EXCHANGE -> "Exchange"
                CREATE_LEASING -> "Create Leasing"
                CANCEL_LEASING -> "Cancel Leasing"
                CREATE_ALIAS -> "Create Alias"
                MASS_TRANSFER -> "MassTransfer"
                DATA -> "Data"
                ADDRESS_SCRIPT -> "Address Script"
                SPONSORSHIP -> "Sponsorship"
                ASSET_SCRIPT -> "Asset Script"
                SCRIPT_INVOCATION -> "Script Invocation"
                else -> ""
            }
        }
    }
}
