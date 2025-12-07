package no.nordicsemi.android.toolbox.profile.view.gci

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject
import no.nordicsemi.android.toolbox.profile.viewmodel.GCIEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.GCIViewModel
import no.nordicsemi.android.toolbox.profile.viewmodel.OTSEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.OTSViewModel

@Composable
internal fun GCIScreen() {
    val gciViewModel = hiltViewModel<GCIViewModel>()
    // val otsServiceData by otsViewModel.otsState.collectAsStateWithLifecycle()
    val onClickEvent: (GCIEvent) -> Unit = { gciViewModel.onEvent(it) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GCIView(
            startClickEvent = { onClickEvent(GCIEvent.StartExercise(1)) },
        )
    }
}

@Composable
private fun GCIView(
    startClickEvent: () -> Unit,
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
                    text = stringResource(id = R.string.gci_label),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = startClickEvent) {
                    Text(text = "Start Exercise")
                }
            }
        }
    }
}