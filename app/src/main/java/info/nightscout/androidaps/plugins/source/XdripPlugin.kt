package info.nightscout.androidaps.plugins.source

import android.content.Intent
import info.nightscout.androidaps.MainApp
import info.nightscout.androidaps.R
import info.nightscout.androidaps.db.BgReading
import info.nightscout.androidaps.interfaces.BgSourceInterface
import info.nightscout.androidaps.interfaces.PluginBase
import info.nightscout.androidaps.interfaces.PluginDescription
import info.nightscout.androidaps.interfaces.PluginType
import info.nightscout.androidaps.logging.AAPSLogger
import info.nightscout.androidaps.logging.BundleLogger
import info.nightscout.androidaps.logging.LTag
import info.nightscout.androidaps.plugins.bus.RxBusWrapper
import info.nightscout.androidaps.services.Intents
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XdripPlugin @Inject constructor(
    rxBus: RxBusWrapper, aapsLogger: AAPSLogger
) : PluginBase(PluginDescription()
    .mainType(PluginType.BGSOURCE)
    .fragmentClass(BGSourceFragment::class.java.name)
    .pluginName(R.string.xdrip)
    .description(R.string.description_source_xdrip),
    rxBus,
    aapsLogger
), BgSourceInterface {

    var advancedFiltering = false

    override fun advancedFilteringSupported(): Boolean {
        return advancedFiltering
    }

    override fun handleNewData(intent: Intent) {
        if (!isEnabled(PluginType.BGSOURCE)) return
        val bundle = intent.extras ?: return
        aapsLogger.debug(LTag.BGSOURCE, "Received xDrip data: " + BundleLogger.log(intent.extras))
        val bgReading = BgReading()
        bgReading.value = bundle.getDouble(Intents.EXTRA_BG_ESTIMATE)
        bgReading.direction = bundle.getString(Intents.EXTRA_BG_SLOPE_NAME)
        bgReading.date = bundle.getLong(Intents.EXTRA_TIMESTAMP)
        bgReading.raw = bundle.getDouble(Intents.EXTRA_RAW)
        val source = bundle.getString(Intents.XDRIP_DATA_SOURCE_DESCRIPTION, "no Source specified")
        setSource(source)
        MainApp.getDbHelper().createIfNotExists(bgReading, "XDRIP")
    }

    fun setSource(source: String) {
        advancedFiltering = source.contains("G5 Native") || source.contains("G6 Native")
    }
}