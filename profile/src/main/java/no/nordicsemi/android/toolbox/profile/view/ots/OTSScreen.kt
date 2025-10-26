package no.nordicsemi.android.toolbox.profile.view.ots

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPFeatures
import no.nordicsemi.android.toolbox.profile.viewmodel.OTSViewModel
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures

@Composable
internal fun OTSScreen() {
    val otsViewModel = hiltViewModel<OTSViewModel>()
    val otsServiceData by otsViewModel.otsState.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OTSFeaturesView(
            features = otsServiceData.otsFeatures
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OTSScreenPreview() {
    OTSScreen()
}

@Composable
private fun OTSFeaturesView(
    features: OTSFeatures?
) {
    var oacpStr = "OACP:"
    if (features != null) {
        val oacp = features.oacp
        if (oacp.create) oacpStr += " " + stringResource(R.string.oacp_create)
        if (oacp.delete) oacpStr += " " + stringResource(R.string.oacp_delete)
        if (oacp.checksum) oacpStr += " " + stringResource(R.string.oacp_checksum)
        if (oacp.execute) oacpStr += " " + stringResource(R.string.oacp_execute)
        if (oacp.read) oacpStr += " " + stringResource(R.string.oacp_read)
        if (oacp.write) oacpStr += " " + stringResource(R.string.oacp_write)
        if (oacp.append) oacpStr += " " + stringResource(R.string.oacp_append)
        if (oacp.truncate) oacpStr += " " + stringResource(R.string.oacp_truncate)
        if (oacp.patch) oacpStr += " " + stringResource(R.string.oacp_patch)
        if (oacp.abort) oacpStr += " " + stringResource(R.string.oacp_abort)
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    OutlinedCard {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    colorFilter = ColorFilter.tint(textColor)
                )
                Text(
                    text = stringResource(id = R.string.ots_features),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = oacpStr,
                    color = textColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OTSFeaturesViewPreview() {
    OTSFeaturesView(
        features = OTSFeatures(
            oacp = OACPFeatures(
                delete = true
            )
        )
    )
}
