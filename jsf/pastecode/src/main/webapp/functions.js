function chooseStyle(title) {
	var i, curstyle, altsheets = [ "" ]
	for (i = 0; (curstyle = document.getElementsByTagName("link")[i]); i++) {
		if (curstyle.getAttribute("rel").toLowerCase() == "alternate stylesheet"
				&& curstyle.getAttribute("title")) {
			curstyle.disabled = true
			if (curstyle.getAttribute("title") == title)
				curstyle.disabled = false
		}
	}
}

function unwrap(elementId) {
	var vardiv = document.getElementById(elementId);
	if (vardiv.style.overflow == "visible") {
		vardiv.style.overflow = "scroll";
		vardiv.style.height = "450px";
		return false;
	}
	if (vardiv.style.overflow == "scroll") {
		vardiv.style.overflow = "visible";
		vardiv.style.height = null;
		return false;
	}
}
