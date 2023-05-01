module skipBo {
	requires java.desktop;
	requires javafx.graphics;
	requires javafx.controls;
	opens driver to javafx.graphics;
}