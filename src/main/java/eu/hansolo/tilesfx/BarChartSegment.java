/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.tilesfx;

import eu.hansolo.tilesfx.events.BarChartEvent;
import eu.hansolo.tilesfx.fonts.Fonts;
import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Locale;


/**
 * User: hansolo
 * Date: 23.12.16
 * Time: 13:10
 */
@DefaultProperty("children")
public class BarChartSegment extends Region implements Comparable<BarChartSegment>{
    private static final double         PREFERRED_WIDTH  = 250;
    private static final double         PREFERRED_HEIGHT = 30;
    private static final double         MINIMUM_WIDTH    = 25;
    private static final double         MINIMUM_HEIGHT   = 3.6;
    private static final double         MAXIMUM_WIDTH    = 1024;
    private static final double         MAXIMUM_HEIGHT   = 1024;
    private static final double         ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private static final BarChartEvent  UPDATE_EVENT     = new BarChartEvent(BarChartEvent.UPDATE);
    private       double                width;
    private       double                height;
    private       Text                  nameText;
    private       Text                  valueText;
    private       Rectangle             barBackground;
    private       Rectangle             bar;
    private       Pane                  pane;
    private       StringProperty        name;
    private       DoubleProperty        value;
    private       ObjectProperty<Color> nameColor;
    private       ObjectProperty<Color> valueColor;
    private       ObjectProperty<Color> barBackgroundColor;
    private       ObjectProperty<Color> barColor;
    private       String                formatString;
    private       Locale                locale;
    private       double                maxValue;
    private       double                stepSize;


    // ******************** Constructors **************************************
    public BarChartSegment(final String NAME) {
        this(NAME, 0, Tile.BLUE);
    }
    public BarChartSegment(final String NAME, final double VALUE) {
        this(NAME, VALUE, Tile.BLUE);
    }
    public BarChartSegment(final String NAME, final double VALUE, final Color COLOR) {
        name               = new StringPropertyBase(NAME) {
            @Override protected void invalidated() { nameText.setText(get()); }
            @Override public Object getBean() { return BarChartSegment.this; }
            @Override public String getName() { return "name"; }
        };
        value              = new DoublePropertyBase(VALUE) {
            @Override protected void invalidated() {
                updateBar(get());
                fireEvent(UPDATE_EVENT);
            }
            @Override public Object getBean() { return BarChartSegment.this; }
            @Override public String getName() { return "value"; }
        };
        nameColor          = new ObjectPropertyBase<Color>(Tile.FOREGROUND) {
            @Override protected void invalidated() { nameText.setFill(get()); }
            @Override public Object getBean() { return BarChartSegment.this; }
            @Override public String getName() { return "nameColor"; }
        };
        valueColor         = new ObjectPropertyBase<Color>(Tile.FOREGROUND) {
            @Override protected void invalidated() {  valueText.setFill(get()); }
            @Override public Object getBean() { return BarChartSegment.this; }
            @Override public String getName() { return "valueColor"; }
        };
        barBackgroundColor = new ObjectPropertyBase<Color>(Color.rgb(72, 72, 72)) {
            @Override protected void invalidated() { barBackground.setFill(get()); }
            @Override public Object getBean() { return BarChartSegment.this; }
            @Override public String getName() { return "barBackgroundColor"; }
        };
        barColor           = new ObjectPropertyBase<Color>(COLOR) {
            @Override protected void invalidated() { bar.setFill(get()); }
            @Override public Object getBean() { return BarChartSegment.this; }
            @Override public String getName() { return "barColor"; }
        };
        formatString       = "%.0f";
        locale             = Locale.US;
        maxValue           = 100;
        stepSize           = PREFERRED_WIDTH * 0.85 / maxValue;
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        nameText = new Text(getName());
        nameText.setTextOrigin(VPos.TOP);

        valueText = new Text(String.format(locale, formatString, getValue()));
        valueText.setTextOrigin(VPos.TOP);

        barBackground = new Rectangle();

        bar = new Rectangle();

        pane = new Pane(nameText, valueText, barBackground, bar);
        pane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public String getName() { return name.get(); }
    public ReadOnlyStringProperty nameProperty() { return name; }

    public double getValue() { return value.get(); }
    public void setValue(final double VALUE) { value.set(VALUE); }
    public DoubleProperty valueProperty() { return value; }

    public Color getNameColor() { return nameColor.get(); }
    public void setNameColor(final Color COLOR) { nameColor.set(COLOR); }
    public ObjectProperty<Color> nameColorProperty() { return nameColor; }

    public Color getValueColor() { return valueColor.get(); }
    public void setValueColor(final Color COLOR) { valueColor.set(COLOR); }
    public ObjectProperty<Color> valueColorProperty() { return valueColor; }

    public Color getBarBackgroundColor() { return barBackgroundColor.get(); }
    public void setBarBackgroundColor(final Color COLOR) { barBackgroundColor.set(COLOR); }
    public ObjectProperty<Color> barBackgroundColorProperty() { return barBackgroundColor; }

    public Color getBarColor() { return barColor.get(); }
    public void setBarColor(final Color COLOR) { barColor.set(COLOR); }
    public ObjectProperty<Color> barColorProperty() { return barColor; }

    @Override public int compareTo(final BarChartSegment SEGMENT) { return Double.compare(getValue(), SEGMENT.getValue()); }

    public void setStepSize(final double STEP_SIZE) {
        stepSize = STEP_SIZE;
        updateBar(getValue());
    }

    public void setMaxValue(final double MAX_VALUE) {
        maxValue = MAX_VALUE;
        stepSize = width * 0.85 / maxValue;
        updateBar(getValue());
    }

    public void setLocale(final Locale LOCALE) {
        locale = LOCALE;
        valueText.setText(String.format(locale, formatString, getValue()));
    }

    public void setFormatString(final String FORMAT_STRING) {
        formatString = FORMAT_STRING;
        valueText.setText(String.format(locale, formatString, getValue()));
    }

    private void updateBar(final double VALUE) {
        valueText.setText(String.format(locale, formatString, VALUE));
        valueText.setX((width * 0.95) - valueText.getLayoutBounds().getWidth());
        bar.setWidth(VALUE * stepSize);
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            stepSize = width * 0.85 / maxValue;

            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            nameText.setFont(Fonts.latoRegular(width * 0.06));
            nameText.setX(width * 0.05);
            nameText.setY(0);

            valueText.setFont(Fonts.latoRegular(width * 0.06));
            valueText.setX((width * 0.95) - valueText.getLayoutBounds().getWidth());
            valueText.setY(0);

            barBackground.setX(width * 0.075);
            barBackground.setY(height * 0.86111111);
            barBackground.setWidth(width * 0.85);
            barBackground.setHeight(height * 0.08333333);

            bar.setX(width * 0.075);
            bar.setY(height * 0.80555556);
            bar.setWidth(getValue() * stepSize);
            bar.setHeight(height * 0.19444444);

            redraw();
        }
    }

    private void redraw() {
        nameText.setFill(getNameColor());
        valueText.setFill(getValueColor());
        barBackground.setFill(getBarBackgroundColor());
        bar.setFill(getBarColor());
    }

    @Override public String toString() {
        return new StringBuilder(getName()).append(",").append(getValue()).append(",").append(getBarColor()).toString();
    }
}
