package com.live2.media.core.exoplayer

import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.SlidingPercentile
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.NetworkTypeObserver
import com.google.android.exoplayer2.util.Util
import com.google.common.base.Ascii
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap

/**
 * Copy of [com.google.android.exoplayer2.upstream.DefaultBandwidthMeter] which allows
 * resetting bitrate estimate via [ResettableBandwidthMeter.resetBitrate]
 */

class ResettableBandwidthMeter private constructor(
    context: Context?,
    initialBitrateEstimates: Map<Int, Long>,
    maxWeight: Int,
    clock: Clock,
    resetOnNetworkTypeChange: Boolean
) : BandwidthMeter, TransferListener {
    /** Builder for a bandwidth meter.  */
    class Builder(context: Context?) {
        private val context: Context?
        private var initialBitrateEstimates: MutableMap<Int, Long>
        private var slidingWindowMaxWeight: Int
        private var clock: Clock
        private var resetOnNetworkTypeChange = true

        /**
         * Creates a builder with default parameters and without listener.
         *
         * @param context A context.
         */
        init {
            // Handling of null is for backward compatibility only.
            this.context = context?.applicationContext
            initialBitrateEstimates =
               getInitialBitrateEstimatesForCountry(
                    Util.getCountryCode(context)
                )
            slidingWindowMaxWeight = DEFAULT_SLIDING_WINDOW_MAX_WEIGHT
            clock = Clock.DEFAULT
        }

        /**
         * Sets the maximum weight for the sliding window.
         *
         * @param slidingWindowMaxWeight The maximum weight for the sliding window.
         * @return This builder.
         */
        fun setSlidingWindowMaxWeight(slidingWindowMaxWeight: Int): Builder {
            this.slidingWindowMaxWeight = slidingWindowMaxWeight
            return this
        }

        /**
         * Sets the initial bitrate estimate in bits per second that should be assumed when a bandwidth
         * estimate is unavailable.
         *
         * @param initialBitrateEstimate The initial bitrate estimate in bits per second.
         * @return This builder.
         */
        fun setInitialBitrateEstimate(initialBitrateEstimate: Long): Builder {
            for (networkType in initialBitrateEstimates.keys) {
                setInitialBitrateEstimate(networkType, initialBitrateEstimate)
            }
            return this
        }

        /**
         * Sets the initial bitrate estimate in bits per second that should be assumed when a bandwidth
         * estimate is unavailable and the current network connection is of the specified type.
         *
         * @param networkType The [C.NetworkType] this initial estimate is for.
         * @param initialBitrateEstimate The initial bitrate estimate in bits per second.
         * @return This builder.
         */

        fun setInitialBitrateEstimate(
            @C.NetworkType
            networkType: Int, initialBitrateEstimate: Long
        ): Builder {
            initialBitrateEstimates[networkType] = initialBitrateEstimate
            return this
        }

        /**
         * Sets the initial bitrate estimates to the default values of the specified country. The
         * initial estimates are used when a bandwidth estimate is unavailable.
         *
         * @param countryCode The ISO 3166-1 alpha-2 country code of the country whose default bitrate
         * estimates should be used.
         * @return This builder.
         */
        fun setInitialBitrateEstimate(countryCode: String?): Builder {
            initialBitrateEstimates =
                countryCode?.let { Ascii.toUpperCase(it) }?.let {
                    getInitialBitrateEstimatesForCountry(
                        it
                    )
                }!!
            return this
        }

        /**
         * Sets the clock used to estimate bandwidth from data transfers. Should only be set for testing
         * purposes.
         *
         * @param clock The clock used to estimate bandwidth from data transfers.
         * @return This builder.
         */
        fun setClock(clock: Clock): Builder {
            this.clock = clock
            return this
        }

        /**
         * Sets whether to reset if the network type changes. The default value is `true`.
         *
         * @param resetOnNetworkTypeChange Whether to reset if the network type changes.
         * @return This builder.
         */
        fun setResetOnNetworkTypeChange(resetOnNetworkTypeChange: Boolean): Builder {
            this.resetOnNetworkTypeChange = resetOnNetworkTypeChange
            return this
        }

        /**
         * Builds the bandwidth meter.
         *
         * @return A bandwidth meter with the configured properties.
         */
        fun build(): ResettableBandwidthMeter {
            return ResettableBandwidthMeter(
                context,
                initialBitrateEstimates,
                slidingWindowMaxWeight,
                clock,
                resetOnNetworkTypeChange
            )
        }

        companion object {
            private fun getInitialBitrateEstimatesForCountry(countryCode: String): MutableMap<Int, Long> {
                val groupIndices = getInitialBitrateCountryGroupAssignment(countryCode)
                val result: MutableMap<Int, Long> = HashMap( /* initialCapacity= */8)
                result[C.NETWORK_TYPE_UNKNOWN] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATE
                result[C.NETWORK_TYPE_WIFI] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATES_WIFI[groupIndices[COUNTRY_GROUP_INDEX_WIFI]]
                result[C.NETWORK_TYPE_2G] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATES_2G[groupIndices[COUNTRY_GROUP_INDEX_2G]]
                result[C.NETWORK_TYPE_3G] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATES_3G[groupIndices[COUNTRY_GROUP_INDEX_3G]]
                result[C.NETWORK_TYPE_4G] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATES_4G[groupIndices[COUNTRY_GROUP_INDEX_4G]]
                result[C.NETWORK_TYPE_5G_NSA] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATES_5G_NSA[groupIndices[COUNTRY_GROUP_INDEX_5G_NSA]]
                result[C.NETWORK_TYPE_5G_SA] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATES_5G_SA[groupIndices[COUNTRY_GROUP_INDEX_5G_SA]]
                // Assume default Wifi speed for Ethernet to prevent using the slower fallback.
                result[C.NETWORK_TYPE_ETHERNET] =
                    DEFAULT_INITIAL_BITRATE_ESTIMATES_WIFI[groupIndices[COUNTRY_GROUP_INDEX_WIFI]]
                return result
            }
        }
    }

    private val initialBitrateEstimates: ImmutableMap<Int, Long>
    private val eventDispatcher: BandwidthMeter.EventListener.EventDispatcher
    private val slidingPercentile: SlidingPercentile
    private val clock: Clock
    private val resetOnNetworkTypeChange: Boolean
    private var streamCount = 0
    private var sampleStartTimeMs: Long = 0
    private var sampleBytesTransferred: Long = 0

    @C.NetworkType
    private var networkType:  Int = 0
    private var totalElapsedTimeMs: Long = 0
    private var totalBytesTransferred: Long = 0
    private var bitrateEstimate: Long = 0
    private var lastReportedBitrateEstimate: Long = 0
    private var networkTypeOverrideSet = false

    @C.NetworkType
    private var networkTypeOverride: Int = 0

    @Deprecated("Use {@link ResettableBandwidthMeter.Builder} instead. ")
    constructor() : this( /* context= */
        null,  /* initialBitrateEstimates= */
        ImmutableMap.of<Int, Long>(),
        DEFAULT_SLIDING_WINDOW_MAX_WEIGHT,
        Clock.DEFAULT,  /* resetOnNetworkTypeChange= */
        false
    )

    init {
        this.initialBitrateEstimates = ImmutableMap.copyOf(initialBitrateEstimates)
        eventDispatcher = BandwidthMeter.EventListener.EventDispatcher()
        slidingPercentile = SlidingPercentile(maxWeight)
        this.clock = clock
        this.resetOnNetworkTypeChange = resetOnNetworkTypeChange
        if (context != null) {
            val networkTypeObserver = NetworkTypeObserver.getInstance(context)
            networkType = networkTypeObserver.networkType
            bitrateEstimate = getInitialBitrateEstimateForNetworkType(networkType)
            networkTypeObserver.register { networkType: Int ->
                onNetworkTypeChanged(
                    networkType
                )
            }
        } else {
            networkType = C.NETWORK_TYPE_UNKNOWN
            bitrateEstimate = getInitialBitrateEstimateForNetworkType(C.NETWORK_TYPE_UNKNOWN)
        }
    }

    /**
     * Overrides the network type. Handled in the same way as if the meter had detected a change from
     * the current network type to the specified network type internally.
     *
     *
     * Applications should not normally call this method. It is intended for testing purposes.
     *
     * @param networkType The overriding network type.
     */
    @Synchronized
    fun setNetworkTypeOverride(@C.NetworkType networkType: Int) {
        networkTypeOverride = networkType
        networkTypeOverrideSet = true
        onNetworkTypeChanged(networkType)
    }

    @Synchronized
    override fun getBitrateEstimate(): Long {
        return bitrateEstimate
    }

    override fun getTransferListener(): TransferListener? {
        return this
    }

    override fun addEventListener(
        eventHandler: Handler,
        eventListener: BandwidthMeter.EventListener
    ) {
        Assertions.checkNotNull(eventHandler)
        Assertions.checkNotNull(eventListener)
        eventDispatcher.addListener(eventHandler, eventListener)
    }

    override fun removeEventListener(eventListener: BandwidthMeter.EventListener) {
        eventDispatcher.removeListener(eventListener)
    }

    override fun onTransferInitializing(
        source: DataSource,
        dataSpec: DataSpec,
        isNetwork: Boolean
    ) {
        // Do nothing.
    }

    @Synchronized
    override fun onTransferStart(
        source: DataSource, dataSpec: DataSpec, isNetwork: Boolean
    ) {
        if (!isTransferAtFullNetworkSpeed(dataSpec, isNetwork)) {
            return
        }
        if (streamCount == 0) {
            sampleStartTimeMs = clock.elapsedRealtime()
        }
        streamCount++
    }

    @Synchronized
    override fun onBytesTransferred(
        source: DataSource, dataSpec: DataSpec, isNetwork: Boolean, bytesTransferred: Int
    ) {
        if (!isTransferAtFullNetworkSpeed(dataSpec, isNetwork)) {
            return
        }
        sampleBytesTransferred += bytesTransferred.toLong()
    }

    @Synchronized
    override fun onTransferEnd(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {
        if (!isTransferAtFullNetworkSpeed(dataSpec, isNetwork)) {
            return
        }
        Assertions.checkState(streamCount > 0)
        val nowMs = clock.elapsedRealtime()
        val sampleElapsedTimeMs = (nowMs - sampleStartTimeMs).toInt()
        totalElapsedTimeMs += sampleElapsedTimeMs.toLong()
        totalBytesTransferred += sampleBytesTransferred
        if (sampleElapsedTimeMs > 0) {
            val bitsPerSecond = sampleBytesTransferred * 8000f / sampleElapsedTimeMs
            slidingPercentile.addSample(
                Math.sqrt(sampleBytesTransferred.toDouble()).toInt(),
                bitsPerSecond
            )
            if (totalElapsedTimeMs >= ELAPSED_MILLIS_FOR_ESTIMATE
                || totalBytesTransferred >= BYTES_TRANSFERRED_FOR_ESTIMATE
            ) {
                bitrateEstimate = slidingPercentile.getPercentile(0.5f).toLong()
            }
            maybeNotifyBandwidthSample(sampleElapsedTimeMs, sampleBytesTransferred, bitrateEstimate)
            sampleStartTimeMs = nowMs
            sampleBytesTransferred = 0
        } // Else any sample bytes transferred will be carried forward into the next sample.
        streamCount--
    }

    @Synchronized
    private fun onNetworkTypeChanged(@C.NetworkType networkType: Int) {
        var networkType = networkType
        if (this.networkType != C.NETWORK_TYPE_UNKNOWN && !resetOnNetworkTypeChange) {
            // Reset on network change disabled. Ignore all updates except the initial one.
            return
        }
        if (networkTypeOverrideSet) {
            networkType = networkTypeOverride
        }
        if (this.networkType == networkType) {
            return
        }
        this.networkType = networkType
        if (networkType == C.NETWORK_TYPE_OFFLINE || networkType == C.NETWORK_TYPE_UNKNOWN || networkType == C.NETWORK_TYPE_OTHER) {
            // It's better not to reset the bandwidth meter for these network types.
            return
        }

        // Reset the bitrate estimate and report it, along with any bytes transferred.
        bitrateEstimate = getInitialBitrateEstimateForNetworkType(networkType)
        val nowMs = clock.elapsedRealtime()
        val sampleElapsedTimeMs = if (streamCount > 0) (nowMs - sampleStartTimeMs).toInt() else 0
        maybeNotifyBandwidthSample(sampleElapsedTimeMs, sampleBytesTransferred, bitrateEstimate)

        // Reset the remainder of the state.
        sampleStartTimeMs = nowMs
        sampleBytesTransferred = 0
        totalBytesTransferred = 0
        totalElapsedTimeMs = 0
        slidingPercentile.reset()
    }

    private fun maybeNotifyBandwidthSample(
        elapsedMs: Int, bytesTransferred: Long, bitrateEstimate: Long
    ) {
        if (elapsedMs == 0 && bytesTransferred == 0L && bitrateEstimate == lastReportedBitrateEstimate) {
            return
        }
        lastReportedBitrateEstimate = bitrateEstimate
        eventDispatcher.bandwidthSample(elapsedMs, bytesTransferred, bitrateEstimate)
    }

    private fun getInitialBitrateEstimateForNetworkType(@C.NetworkType networkType: Int): Long {
        var initialBitrateEstimate = initialBitrateEstimates[networkType]
        if (initialBitrateEstimate == null) {
            initialBitrateEstimate = initialBitrateEstimates[C.NETWORK_TYPE_UNKNOWN]
        }
        if (initialBitrateEstimate == null) {
            initialBitrateEstimate = DEFAULT_INITIAL_BITRATE_ESTIMATE
        }
        return initialBitrateEstimate
    }

    /**
     * Resets bitrate to initial value provided in the builder
     */
    fun resetBitrate() {
        bitrateEstimate = getInitialBitrateEstimateForNetworkType(networkType)
    }

    companion object {
        /** Default initial Wifi bitrate estimate in bits per second.  */
        val DEFAULT_INITIAL_BITRATE_ESTIMATES_WIFI =
            ImmutableList.of(5400000L, 3300000L, 2000000L, 1300000L, 760000L)

        /** Default initial 2G bitrate estimates in bits per second.  */
        val DEFAULT_INITIAL_BITRATE_ESTIMATES_2G =
            ImmutableList.of(1700000L, 820000L, 450000L, 180000L, 130000L)

        /** Default initial 3G bitrate estimates in bits per second.  */
        val DEFAULT_INITIAL_BITRATE_ESTIMATES_3G =
            ImmutableList.of(2300000L, 1300000L, 1000000L, 820000L, 570000L)

        /** Default initial 4G bitrate estimates in bits per second.  */
        val DEFAULT_INITIAL_BITRATE_ESTIMATES_4G =
            ImmutableList.of(3400000L, 2000000L, 1400000L, 1000000L, 620000L)

        /** Default initial 5G-NSA bitrate estimates in bits per second.  */
        val DEFAULT_INITIAL_BITRATE_ESTIMATES_5G_NSA =
            ImmutableList.of(7500000L, 5200000L, 3700000L, 1800000L, 1100000L)

        /** Default initial 5G-SA bitrate estimates in bits per second.  */
        val DEFAULT_INITIAL_BITRATE_ESTIMATES_5G_SA =
            ImmutableList.of(3300000L, 1900000L, 1700000L, 1500000L, 1200000L)

        /**
         * Default initial bitrate estimate used when the device is offline or the network type cannot be
         * determined, in bits per second.
         */
        const val DEFAULT_INITIAL_BITRATE_ESTIMATE: Long = 1000000

        /** Default maximum weight for the sliding window.  */
        const val DEFAULT_SLIDING_WINDOW_MAX_WEIGHT = 2000

        /**
         * Index for the Wifi group index in the array returned by [ ][.getInitialBitrateCountryGroupAssignment].
         */
        private const val COUNTRY_GROUP_INDEX_WIFI = 0

        /**
         * Index for the 2G group index in the array returned by [ ][.getInitialBitrateCountryGroupAssignment].
         */
        private const val COUNTRY_GROUP_INDEX_2G = 1

        /**
         * Index for the 3G group index in the array returned by [ ][.getInitialBitrateCountryGroupAssignment].
         */
        private const val COUNTRY_GROUP_INDEX_3G = 2

        /**
         * Index for the 4G group index in the array returned by [ ][.getInitialBitrateCountryGroupAssignment].
         */
        private const val COUNTRY_GROUP_INDEX_4G = 3

        /**
         * Index for the 5G-NSA group index in the array returned by [ ][.getInitialBitrateCountryGroupAssignment].
         */
        private const val COUNTRY_GROUP_INDEX_5G_NSA = 4

        /**
         * Index for the 5G-SA group index in the array returned by [ ][.getInitialBitrateCountryGroupAssignment].
         */
        private const val COUNTRY_GROUP_INDEX_5G_SA = 5
        private var singletonInstance: ResettableBandwidthMeter? = null

        /**
         * Returns a singleton instance of a [ResettableBandwidthMeter] with default configuration.
         *
         * @param context A [Context].
         * @return The singleton instance.
         */
        @Synchronized
        fun getSingletonInstance(context: Context?): ResettableBandwidthMeter? {
            if (singletonInstance == null) {
                singletonInstance =
                   Builder(context).build()
            }
            return singletonInstance
        }

        private const val ELAPSED_MILLIS_FOR_ESTIMATE = 2000
        private const val BYTES_TRANSFERRED_FOR_ESTIMATE = 512 * 1024
        private fun isTransferAtFullNetworkSpeed(dataSpec: DataSpec, isNetwork: Boolean): Boolean {
            return isNetwork && !dataSpec.isFlagSet(DataSpec.FLAG_MIGHT_NOT_USE_FULL_NETWORK_SPEED)
        }

        /**
         * Returns initial bitrate group assignments for a `country`. The initial bitrate is a list
         * of indices for [Wifi, 2G, 3G, 4G, 5G_NSA, 5G_SA].
         */
        private fun getInitialBitrateCountryGroupAssignment(country: String): IntArray {
            return when (country) {
                "AE" -> intArrayOf(1, 4, 4, 4, 3, 2)
                "AG" -> intArrayOf(2, 3, 1, 2, 2, 2)
                "AM" -> intArrayOf(2, 3, 2, 4, 2, 2)
                "AR" -> intArrayOf(2, 4, 1, 1, 2, 2)
                "AS" -> intArrayOf(2, 2, 2, 3, 2, 2)
                "AU" -> intArrayOf(0, 1, 0, 1, 2, 2)
                "BE" -> intArrayOf(0, 0, 3, 3, 2, 2)
                "BF" -> intArrayOf(4, 3, 4, 3, 2, 2)
                "BH" -> intArrayOf(1, 2, 2, 4, 4, 2)
                "BJ" -> intArrayOf(4, 4, 3, 4, 2, 2)
                "BN" -> intArrayOf(3, 2, 1, 1, 2, 2)
                "BO" -> intArrayOf(1, 3, 3, 2, 2, 2)
                "BQ" -> intArrayOf(1, 2, 2, 0, 2, 2)
                "BS" -> intArrayOf(4, 2, 2, 3, 2, 2)
                "BT" -> intArrayOf(3, 1, 3, 2, 2, 2)
                "BY" -> intArrayOf(0, 1, 1, 3, 2, 2)
                "BZ" -> intArrayOf(2, 4, 2, 2, 2, 2)
                "CA" -> intArrayOf(0, 2, 1, 2, 4, 1)
                "CD" -> intArrayOf(4, 2, 3, 1, 2, 2)
                "CF" -> intArrayOf(4, 2, 3, 2, 2, 2)
                "CI" -> intArrayOf(3, 3, 3, 4, 2, 2)
                "CK" -> intArrayOf(2, 2, 2, 1, 2, 2)
                "AO", "CM" -> intArrayOf(3, 4, 3, 2, 2, 2)
                "CN" -> intArrayOf(2, 0, 2, 2, 3, 1)
                "CO" -> intArrayOf(2, 2, 4, 2, 2, 2)
                "CR" -> intArrayOf(2, 2, 4, 4, 2, 2)
                "CV" -> intArrayOf(2, 3, 1, 0, 2, 2)
                "CW" -> intArrayOf(2, 2, 0, 0, 2, 2)
                "CY" -> intArrayOf(1, 0, 0, 0, 1, 2)
                "DE" -> intArrayOf(0, 0, 2, 2, 1, 2)
                "DJ" -> intArrayOf(4, 1, 4, 4, 2, 2)
                "DK" -> intArrayOf(0, 0, 1, 0, 0, 2)
                "EC" -> intArrayOf(2, 4, 2, 1, 2, 2)
                "EG" -> intArrayOf(3, 4, 2, 3, 2, 2)
                "ET" -> intArrayOf(4, 4, 3, 1, 2, 2)
                "FI" -> intArrayOf(0, 0, 0, 1, 0, 2)
                "FJ" -> intArrayOf(3, 1, 3, 3, 2, 2)
                "FM" -> intArrayOf(3, 2, 4, 2, 2, 2)
                "FR" -> intArrayOf(1, 1, 2, 1, 1, 1)
                "GA" -> intArrayOf(2, 3, 1, 1, 2, 2)
                "GB" -> intArrayOf(0, 0, 1, 1, 2, 3)
                "GE" -> intArrayOf(1, 1, 1, 3, 2, 2)
                "BB", "FO", "GG" -> intArrayOf(0, 2, 0, 0, 2, 2)
                "GH" -> intArrayOf(3, 2, 3, 2, 2, 2)
                "GN" -> intArrayOf(4, 3, 4, 2, 2, 2)
                "GQ" -> intArrayOf(4, 2, 3, 4, 2, 2)
                "GT" -> intArrayOf(2, 3, 2, 1, 2, 2)
                "AW", "GU" -> intArrayOf(1, 2, 4, 4, 2, 2)
                "BW", "GY" -> intArrayOf(3, 4, 1, 0, 2, 2)
                "HK" -> intArrayOf(0, 1, 2, 3, 2, 0)
                "HU" -> intArrayOf(0, 0, 0, 1, 3, 2)
                "ID" -> intArrayOf(3, 2, 3, 3, 3, 2)
                "ES", "IE" -> intArrayOf(0, 1, 1, 1, 2, 2)
                "IL" -> intArrayOf(1, 1, 2, 3, 4, 2)
                "IM" -> intArrayOf(0, 2, 0, 1, 2, 2)
                "IN" -> intArrayOf(1, 1, 3, 2, 4, 3)
                "IR" -> intArrayOf(3, 0, 1, 1, 3, 0)
                "IT" -> intArrayOf(0, 1, 0, 1, 1, 2)
                "JE" -> intArrayOf(3, 2, 1, 2, 2, 2)
                "DO", "JM" -> intArrayOf(3, 4, 4, 4, 2, 2)
                "JP" -> intArrayOf(0, 1, 0, 1, 1, 1)
                "KE" -> intArrayOf(3, 3, 2, 2, 2, 2)
                "KG" -> intArrayOf(2, 1, 1, 1, 2, 2)
                "KH" -> intArrayOf(1, 1, 4, 2, 2, 2)
                "KR" -> intArrayOf(0, 0, 1, 3, 4, 4)
                "KW" -> intArrayOf(1, 1, 0, 0, 0, 2)
                "AL", "BA", "KY" -> intArrayOf(1, 2, 0, 1, 2, 2)
                "KZ" -> intArrayOf(1, 1, 2, 2, 2, 2)
                "LB" -> intArrayOf(3, 2, 1, 4, 2, 2)
                "AD", "BM", "GL", "LC" -> intArrayOf(1, 2, 0, 0, 2, 2)
                "LK" -> intArrayOf(3, 1, 3, 4, 4, 2)
                "LR" -> intArrayOf(3, 4, 4, 3, 2, 2)
                "LS" -> intArrayOf(3, 3, 4, 3, 2, 2)
                "LU" -> intArrayOf(1, 0, 2, 2, 2, 2)
                "MC" -> intArrayOf(0, 2, 2, 0, 2, 2)
                "JO", "ME" -> intArrayOf(1, 0, 0, 1, 2, 2)
                "MF" -> intArrayOf(1, 2, 1, 0, 2, 2)
                "MG" -> intArrayOf(3, 4, 2, 2, 2, 2)
                "MH" -> intArrayOf(3, 2, 2, 4, 2, 2)
                "ML" -> intArrayOf(4, 3, 3, 1, 2, 2)
                "MM" -> intArrayOf(2, 4, 3, 3, 2, 2)
                "MN" -> intArrayOf(2, 0, 1, 2, 2, 2)
                "MO" -> intArrayOf(0, 2, 4, 4, 2, 2)
                "GF", "GP", "MQ" -> intArrayOf(2, 1, 2, 3, 2, 2)
                "MR" -> intArrayOf(4, 1, 3, 4, 2, 2)
                "EE", "LT", "LV", "MT" -> intArrayOf(0, 0, 0, 0, 2, 2)
                "MU" -> intArrayOf(3, 1, 1, 2, 2, 2)
                "MV" -> intArrayOf(3, 4, 1, 4, 2, 2)
                "MW" -> intArrayOf(4, 2, 1, 0, 2, 2)
                "CG", "MX" -> intArrayOf(2, 4, 3, 4, 2, 2)
                "BD", "MY" -> intArrayOf(2, 1, 3, 3, 2, 2)
                "NA" -> intArrayOf(4, 3, 2, 2, 2, 2)
                "AZ", "NC" -> intArrayOf(3, 2, 4, 4, 2, 2)
                "NG" -> intArrayOf(3, 4, 1, 1, 2, 2)
                "NI" -> intArrayOf(2, 3, 4, 3, 2, 2)
                "NL" -> intArrayOf(0, 0, 3, 2, 0, 4)
                "NO" -> intArrayOf(0, 0, 2, 0, 0, 2)
                "NP" -> intArrayOf(2, 1, 4, 3, 2, 2)
                "NR" -> intArrayOf(3, 2, 2, 0, 2, 2)
                "NZ" -> intArrayOf(1, 0, 1, 2, 4, 2)
                "OM" -> intArrayOf(2, 3, 1, 3, 4, 2)
                "PA" -> intArrayOf(1, 3, 3, 3, 2, 2)
                "PE" -> intArrayOf(2, 3, 4, 4, 4, 2)
                "PF" -> intArrayOf(2, 3, 3, 1, 2, 2)
                "CU", "PG" -> intArrayOf(4, 4, 3, 2, 2, 2)
                "PH" -> intArrayOf(2, 2, 3, 3, 3, 2)
                "PR" -> intArrayOf(2, 3, 2, 2, 3, 3)
                "PS" -> intArrayOf(3, 4, 1, 2, 2, 2)
                "PT" -> intArrayOf(0, 1, 0, 0, 2, 2)
                "PW" -> intArrayOf(2, 2, 4, 1, 2, 2)
                "PY" -> intArrayOf(2, 2, 3, 2, 2, 2)
                "QA" -> intArrayOf(2, 4, 2, 4, 4, 2)
                "RE" -> intArrayOf(1, 1, 1, 2, 2, 2)
                "RO" -> intArrayOf(0, 0, 1, 1, 1, 2)
                "GR", "HR", "MD", "MK", "RS" -> intArrayOf(1, 0, 0, 0, 2, 2)
                "RU" -> intArrayOf(0, 0, 0, 1, 2, 2)
                "RW" -> intArrayOf(3, 4, 3, 0, 2, 2)
                "KI", "KM", "LY", "SB" -> intArrayOf(4, 2, 4, 3, 2, 2)
                "SC" -> intArrayOf(4, 3, 0, 2, 2, 2)
                "SG" -> intArrayOf(1, 1, 2, 3, 1, 4)
                "BG", "CZ", "SI" -> intArrayOf(0, 0, 0, 0, 1, 2)
                "AT", "CH", "IS", "SE", "SK" -> intArrayOf(0, 0, 0, 0, 0, 2)
                "SL" -> intArrayOf(4, 3, 4, 1, 2, 2)
                "AX", "GI", "LI", "MP", "PM", "SJ", "SM" -> intArrayOf(0, 2, 2, 2, 2, 2)
                "HN", "PK", "SO" -> intArrayOf(3, 2, 3, 3, 2, 2)
                "BR", "SR" -> intArrayOf(2, 3, 2, 2, 2, 2)
                "FK", "KP", "MA", "MZ", "ST" -> intArrayOf(3, 2, 2, 2, 2, 2)
                "SV" -> intArrayOf(2, 2, 3, 3, 2, 2)
                "SZ" -> intArrayOf(4, 3, 2, 4, 2, 2)
                "SX", "TC" -> intArrayOf(2, 2, 1, 0, 2, 2)
                "TG" -> intArrayOf(3, 3, 2, 0, 2, 2)
                "TH" -> intArrayOf(0, 3, 2, 3, 3, 0)
                "TJ" -> intArrayOf(4, 2, 4, 4, 2, 2)
                "BI", "DZ", "SY", "TL" -> intArrayOf(4, 3, 4, 4, 2, 2)
                "TM" -> intArrayOf(4, 2, 4, 2, 2, 2)
                "TO" -> intArrayOf(4, 2, 3, 3, 2, 2)
                "TR" -> intArrayOf(1, 1, 0, 1, 2, 2)
                "TT" -> intArrayOf(1, 4, 1, 1, 2, 2)
                "AQ", "ER", "IO", "NU", "SH", "SS", "TV" -> intArrayOf(4, 2, 2, 2, 2, 2)
                "TW" -> intArrayOf(0, 0, 0, 0, 0, 0)
                "GW", "TZ" -> intArrayOf(3, 4, 3, 3, 2, 2)
                "UA" -> intArrayOf(0, 3, 1, 1, 2, 2)
                "IQ", "UG" -> intArrayOf(3, 3, 3, 3, 2, 2)
                "CL", "PL", "US" -> intArrayOf(1, 1, 2, 2, 3, 2)
                "LA", "UY" -> intArrayOf(2, 2, 1, 2, 2, 2)
                "UZ" -> intArrayOf(2, 2, 3, 4, 2, 2)
                "AI", "BL", "CX", "DM", "GD", "MS", "VC" -> intArrayOf(1, 2, 2, 2, 2, 2)
                "SA", "TN", "VG" -> intArrayOf(2, 2, 1, 1, 2, 2)
                "VI" -> intArrayOf(1, 2, 1, 3, 2, 2)
                "VN" -> intArrayOf(0, 3, 3, 4, 2, 2)
                "VU" -> intArrayOf(4, 2, 2, 1, 2, 2)
                "GM", "WF" -> intArrayOf(4, 2, 2, 4, 2, 2)
                "WS" -> intArrayOf(3, 1, 2, 1, 2, 2)
                "XK" -> intArrayOf(1, 1, 1, 1, 2, 2)
                "AF", "HT", "NE", "SD", "SN", "TD", "VE", "YE" -> intArrayOf(4, 4, 4, 4, 2, 2)
                "YT" -> intArrayOf(4, 1, 1, 1, 2, 2)
                "ZA" -> intArrayOf(3, 3, 1, 1, 1, 2)
                "ZM" -> intArrayOf(3, 3, 4, 2, 2, 2)
                "ZW" -> intArrayOf(3, 2, 4, 3, 2, 2)
                else -> intArrayOf(2, 2, 2, 2, 2, 2)
            }
        }
    }
}
