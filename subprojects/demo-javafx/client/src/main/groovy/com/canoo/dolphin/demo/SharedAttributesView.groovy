package com.canoo.dolphin.demo

import com.canoo.dolphin.core.client.ClientAttribute
import com.canoo.dolphin.core.client.ClientAttributeWrapper
import com.canoo.dolphin.core.client.ClientPresentationModel
import com.canoo.dolphin.core.client.ClientDolphin
import com.canoo.dolphin.core.client.comm.OnFinishedHandler
import com.canoo.dolphin.core.comm.NamedCommand
import groovyx.javafx.SceneGraphBuilder
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.util.Callback

import java.beans.PropertyChangeListener

import static com.canoo.dolphin.binding.JFXBinder.bind
import static com.canoo.dolphin.demo.DemoStyle.blueStyle
import static com.canoo.dolphin.demo.VehicleProperties.*
import static groovyx.javafx.GroovyFX.start
import com.canoo.dolphin.core.client.comm.WithPresentationModelHandler

class SharedAttributesView {

    static show(ClientDolphin clientDolphin) {

        def communicator = clientDolphin.clientConnector

        def selectedVehicle = new ClientPresentationModel('selectedVehicle', [new ClientAttribute('vehiclePmId')])
        clientDolphin.clientModelStore.add selectedVehicle

        ObservableList<ClientPresentationModel> observableListOfPms = FXCollections.observableArrayList()
        ObservableList<ClientPresentationModel> observableListOfTasks = FXCollections.observableArrayList()

        start { app ->
            SceneGraphBuilder sgb = delegate
            stage {
                scene width: 700, height: 500, {
                    borderPane {
                        left margin: 10, {
                            tableView(id: 'table', opacity: 0.2d) {
                                tableColumn(property: 'id', text: "Color", prefWidth: 50)
                                xCol = tableColumn(text: 'X', prefWidth: 40)
                                yCol = tableColumn(text: 'Y', prefWidth: 40)
                                rotCol = tableColumn(text: 'Angle')
                            }
                        }
                        center margin: [10, 0, 10, 0], {
                            tabPane id: 'vehicles'
                        }
                        right margin: 10, {
                            tableView(id: 'taskTable', opacity: 0.2d) {
                                tableColumn(property: 'id', text: "descr", prefWidth: 100)
                                vehicleFillCol = tableColumn(text: 'Vehicle Color', prefWidth: 50)
                                vehicleXCol = tableColumn(text: 'Vehicle X', prefWidth: 50)
                            }
                        }
                    }
                }
            }

            table.items = observableListOfPms
            taskTable.items = observableListOfTasks

            // auto-update the cell values
            xCol.cellValueFactory = { return new ClientAttributeWrapper(it.value[ATT_X]) } as Callback
            yCol.cellValueFactory = { return new ClientAttributeWrapper(it.value[ATT_Y]) } as Callback
            rotCol.cellValueFactory = { return new ClientAttributeWrapper(it.value[ATT_ROTATE]) } as Callback

            vehicleFillCol.cellValueFactory = { return new ClientAttributeWrapper(it.value[ATT_COLOR]) } as Callback
            vehicleXCol.cellValueFactory = { return new ClientAttributeWrapper(it.value[ATT_X]) } as Callback

            // startup and main loop

            communicator.send(new NamedCommand(id: 'pullVehicles'), { pms ->
                for (pm in pms) {
                    observableListOfPms << pm
                }
                fadeTransition(1.s, node: table, to: 1).playFromStart()
            } as OnFinishedHandler )

            communicator.send(new NamedCommand(id: 'pullTasks'), { pms ->
                for (pm in pms) {
                    observableListOfTasks << pm
                }
                fadeTransition(1.s, node: taskTable, to: 1).playFromStart()
            } as OnFinishedHandler )

            blueStyle sgb

            // all the bindings ...

            // bind 'selectedItem' of table.selectionModel to { ... }
            table.selectionModel.selectedItemProperty().addListener({ o, oldVal, selectedPm ->
                selectedVehicle.vehiclePmId.value = selectedPm.id
            } as ChangeListener)

            taskTable.selectionModel.selectedItemProperty().addListener({ o, oldVal, selectedPm ->
                selectedVehicle.vehiclePmId.value = selectedPm.fill.value
            } as ChangeListener)

            selectedVehicle.vehiclePmId.addPropertyChangeListener('value', { evt ->
                def selectedPmId = evt.newValue
                def tab = sgb.vehicles.tabs.find { it.id == selectedPmId }
                if (!tab) {
                    def grid
                    tab = sgb.tab id: selectedPmId, {
                        grid = gridPane hgap: 5, vgap: 5, padding: 10, alignment: "top_left", opacity: 0.3d, translateY: -200, {
                            columnConstraints(halignment: "right")
                            text ' X:', row: 0, column: 0
                            textField id: 'x', prefColumnCount: 3, row: 0, column: 1
                            text ' Y:', row: 1, column: 0
                            textField id: 'y', prefColumnCount: 3, row: 1, column: 1
                            text ' Angle:', row: 2, column: 0
                            textField id: 'angle', prefColumnCount: 3, row: 2, column: 1
                            text ' Width:', row: 3, column: 0
                            textField id: 'width', prefColumnCount: 3, row: 3, column: 1
                            translateTransition(0.5.s, to: 0).playFromStart()
                        }
                    }

                    clientDolphin.clientModelStore.withPresentationModel 'vehicleDetail-'+selectedPmId, { ClientPresentationModel detailPm ->
                        assert detailPm

                        bind ATT_COLOR of detailPm to 'text' of tab

                        bind ATT_X of detailPm to 'text' of sgb.x
                        bind 'text' of sgb.x to ATT_X of detailPm

                        bind ATT_Y of detailPm to 'text' of sgb.y
                        bind ATT_ROTATE of detailPm to 'text' of sgb.angle

                        bind ATT_WIDTH of detailPm to 'text' of sgb.width
                        bind 'text' of sgb.width to ATT_WIDTH of detailPm

                        fadeTransition(1.s, node: grid, to: 1).playFromStart()
                    } as WithPresentationModelHandler
                    sgb.vehicles.tabs << tab
                }
                sgb.vehicles.selectionModel.select(tab)
            } as PropertyChangeListener)

            primaryStage.show()
        }
    }
}
