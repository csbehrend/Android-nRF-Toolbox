package no.nordicsemi.android.toolbox.profile.view.ots

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.Button
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
import no.nordicsemi.android.toolbox.profile.manager.repository.OTSRepository
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResponse
import no.nordicsemi.android.toolbox.profile.viewmodel.OTSViewModel
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject
import no.nordicsemi.android.toolbox.profile.viewmodel.OTSEvent

@Composable
internal fun OTSScreen() {
    val otsViewModel = hiltViewModel<OTSViewModel>()
    val otsServiceData by otsViewModel.otsState.collectAsStateWithLifecycle()
    val onClickEvent: (OTSEvent) -> Unit = { otsViewModel.onEvent(it) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OTSFeaturesView(
            features = otsServiceData.otsFeatures
        )
        OTSObjectView(
            obj = otsServiceData.otsObject
        )
        OLCPView(
            response = otsServiceData.olcpResponse,
            firstClickEvent = { onClickEvent(OTSEvent.OnOLCPRequest(OLCPOperation.First)) },
            lastClickEvent = { onClickEvent(OTSEvent.OnOLCPRequest(OLCPOperation.Last)) },
            prevClickEvent = { onClickEvent(OTSEvent.OnOLCPRequest(OLCPOperation.Previous)) },
            nextClickEvent = { onClickEvent(OTSEvent.OnOLCPRequest(OLCPOperation.Next)) },
        )
        OACPView(
            response = otsServiceData.oacpResponse,
            data = otsServiceData.readData,
            readClickEvent = { onClickEvent(OTSEvent.ReadObject) },
            writeRangeClickEvent = { onClickEvent(OTSEvent.WriteRange) }
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
    features: OTSFeatures
) {
    var oacpStr = "OACP:"
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

    var olcpStr = "OLCP:"
    val olcp = features.olcp
    if (olcp.goto) olcpStr += " " + stringResource(R.string.olcp_goto)
    if (olcp.order) olcpStr += " " + stringResource(R.string.olcp_order)
    if (olcp.reqObjCount) olcpStr += " " + stringResource(R.string.olcp_reqObjCount)
    if (olcp.clearMarking) olcpStr += " " + stringResource(R.string.olcp_clearMarking)

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = olcpStr,
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun OTSObjectView(
    obj: OTSObject
) {
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
                    text = stringResource(id = R.string.ots_object),
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
                    text = "Name: " + (obj.name ?: "N/A"),
                    color = textColor,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Current/Allocated Size: " + obj.size?.current + "/" + obj.size?.allocated,
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun OLCPView(
    response: OLCPResponse,
    firstClickEvent: () -> Unit,
    lastClickEvent: () -> Unit,
    prevClickEvent: () -> Unit,
    nextClickEvent: () -> Unit,
) {
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
                    text = stringResource(id = R.string.olcp_status),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = firstClickEvent) {
                    Text(text = "First")
                }
                Button(onClick = lastClickEvent) {
                    Text(text = "Last")
                }
                Button(onClick = prevClickEvent) {
                    Text(text = "Previous")
                }
                Button(onClick = nextClickEvent) {
                    Text(text = "Next")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Response: $response",
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun OACPView(
    response: OACPResponse,
    data: String,
    readClickEvent: () -> Unit,
    writeRangeClickEvent: () -> Unit,
) {
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
                    text = stringResource(id = R.string.oacp_status),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = readClickEvent) {
                    Text(text = "Read Object")
                }
                Button(onClick = writeRangeClickEvent) {
                    Text(text = "Write Object Range")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Response: $response",
                    color = textColor,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Data Length: ${data.length / 2}",
                    color = textColor,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Data: $data",
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
