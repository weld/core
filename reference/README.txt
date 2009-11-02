NOTE TO EDITORS
===============

Please honor the formatting that has been applied to these documents so that it is easy to track the changes that are
made between commits. The formatting is described using VIM modelines, which appear in a comment before the final end
tag in each document:

<!--
vim:et:ts=3:sw=3:tw=120
-->

They describe the following rules:

 - Expand tabs to spaces
 - indent size: 3 spaces
 - max line length: 120

Also, each node generation should be indented from its parent.

Generally, the text in paragraphs should appear on separate lines from the <para> tags, but there are some cases when
this formatting rule has been relaxed.

Bug in IcedTea
==============

There appears to be a bug in IcedTea on Ubuntu that causes the docbook transformation to fail when inserting an image
into the document (there are several in the reference guide). See this bug for more details.

http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=447951

Please use the Sun Java JDK if you encounter this problem.

vim:et:ts=3:sw=3:tw=120
