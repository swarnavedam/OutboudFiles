# OutboudFiles

Health Assessments survey one QA (question-answer) and one TS (total score) outbound files are 
present on Crush SFTP for every day. (Crush path: /data_mgmt/outbound/ha/)

This program downloads these files locally from Crush server and considates all TS files into a
single file and all QA files into another single file for audit purposes. 
The header and footer of all files are removed keeping only one header.
An extra field is added for each record describing the source file name.
