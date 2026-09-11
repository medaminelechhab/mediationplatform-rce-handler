## Description

Steps to follow to run the integration test for the RCE batch.


## Executing test

To run the integration test for RCE, please rename all files in integration-test-files.tgz by adding today's date, Example: GPE-CCIAL-ORG_20231019, i.e. the test has just been run on the date 20231019 ,
Next, edit the modification dates in the GPE-CCIAL-ORG_YYYYMMDD, ENT-REF-XL2_YYYYMMDD, and ETA-REF-XL2_YYYYMMDD files and upload the file in .tgz format to EFiles.
Then launch the test with the "integration" profile.

To delete the group created in CL, add a deletion date in the GPE-CCIAL-ORG_YYYYMMDD file, and to modify the enterprise or establishment, change the values (for example : the name) in the ENT-REF-XL2_YYYYMMDD, and ETA-REF-XL2_YYYYMMDD files , then reload the file in .tgz format to EFiles, run the test with profile "integration" and check in CL
