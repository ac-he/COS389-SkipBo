module skipBo {
	requires java.desktop;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.compiler;
	opens driver to javafx.graphics;
}