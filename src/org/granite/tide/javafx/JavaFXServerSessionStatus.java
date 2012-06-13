package org.granite.tide.javafx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.stage.Stage;

import org.granite.tide.server.ServerSession.Status;


public class JavaFXServerSessionStatus implements Status {
	
	private Stage stage = null;
	
	private BooleanProperty busy = new ReadOnlyBooleanWrapper(this, "busy", false);
	private BooleanProperty connected = new ReadOnlyBooleanWrapper(this, "connected", false);
	private BooleanProperty showBusyCursor = new SimpleBooleanProperty(this, "showBusyCursor", true);
	
	
	public JavaFXServerSessionStatus() {
		busy.addListener(new ChangeListener<Boolean>() {
			
			private Cursor saveCursor = Cursor.DEFAULT;
			
			@Override
			public void changed(ObservableValue<? extends Boolean> property, Boolean oldValue, Boolean newValue) {
				if (stage != null && stage.getScene() != null && showBusyCursor.get()) {
					if (Boolean.FALSE.equals(oldValue)) {
						saveCursor = stage.getScene().getCursor();
						stage.getScene().setCursor(Cursor.WAIT);
					}
					else
						stage.getScene().setCursor(saveCursor);
				}
			}
		});
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	
	public ReadOnlyBooleanProperty busyProperty() {
		return busy;
	}
	
	public ReadOnlyBooleanProperty connectedProperty() {
		return connected;
	}
	
	public BooleanProperty showBusyCursorProperty() {
		return showBusyCursor;
	}

	@Override
	public boolean isBusy() {
		return busy.get();
	}

	@Override
	public void setBusy(boolean busy) {
		this.busy.set(busy);
	}

	@Override
	public boolean isConnected() {
		return connected.get();
	}

	@Override
	public void setConnected(boolean connected) {
		this.connected.set(connected);
	}

	@Override
	public boolean isShowBusyCursor() {
		return showBusyCursor.get();
	}

	@Override
	public void setShowBusyCursor(boolean showBusyCursor) {
		this.showBusyCursor.set(showBusyCursor);
	}

}
