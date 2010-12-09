Guide to Translation Build Scripts
==================================


Translation Workflow
--------------------

1. Author modifies documentation, checks in DocBook XML source.
2. At some point in the lifecycle, a documentation freeze is announced.
3. Import job is run (eg from Hudson).  (See script 2 below: `flies_import_source`.)
4. Translators can begin translating at <https://translate.jboss.org/flies/>.
5. Draft builds are run nightly or more often (Hudson?).  (See script 3 below: `flies_draft_build`)
6. If author changes any XML, go back to step 3.
7. Translations declared "final"
8. Documentation release build is run.  (See script 4 below: `flies_export_translations`)



Configuration for build machine
-------------------------------
Create the file `~/.config/flies.ini` like this:

<pre>
[servers]
jboss.url = https://translate.jboss.org/flies/
jboss.username = your_jboss_username
jboss.key = your_API_key_from_Flies_Profile_page
</pre>

NB: Your key can be obtained by logging in to [Flies](https://translate.jboss.org/flies/), 
visiting the [Profile page](https://translate.jboss.org/flies/profile/view) and 
clicking "generate API key" at the bottom.

The Scripts
-----------

1. Initial Import (run once only, when first integrating with Flies):  
 This script will update the POT (source) and PO (translation) files 
 under `src/main/docbook` from the DocBook XML, and push the content 
 to Flies for translation.  
 `src/main/scripts/flies_import_all`  
 `git checkin src/main/docbook`  

2. Import job (run after documentation freeze):  
 This script will update the POT files in `src/main/docbook/pot` and
 push them to Flies for translation.  
 `src/main/scripts/flies_import_source`

3. Build docs with latest translations (probably run nightly):  
 This script will fetch the latest translations to temporary files 
 in `target/draft` and build the documentation for review purposes.  
 `src/main/scripts/flies_draft_build`

4. Documentation release build:  
 This script will fetch the latest translations so that the release 
 build can be run.  
 `src/main/scripts/flies_export_translations`  
 `git checkin src/main/docbook`  
 _run existing ant build_


