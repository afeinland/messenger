COPY USER_LIST
FROM '/class/classes/lgarc025/messenger/data/usr_list.csv'
WITH DELIMITER ';';
ALTER SEQUENCE user_list_list_id_seq RESTART 55906;

COPY USR

FROM '/class/classes/lgarc025/messenger/data/usr.csv'
WITH DELIMITER ';';

COPY USER_LIST_CONTAINS

FROM '/class/classes/lgarc025/messenger/data/usr_list_contains.csv'
WITH DELIMITER ';';

COPY CHAT
FROM '/class/classes/lgarc025/messenger/data/chat.csv'
WITH DELIMITER ';';
ALTER SEQUENCE chat_chat_id_seq RESTART 5001;

COPY CHAT_LIST
FROM '/class/classes/lgarc025/messenger/data/chat_list.csv'
WITH DELIMITER ';';

COPY MESSAGE
	(msg_id, 
	msg_text, 
	msg_timestamp, 
	sender_login,
	chat_id)

FROM '/class/classes/lgarc025/messenger/data/message.csv'
WITH DELIMITER ';';
ALTER SEQUENCE message_msg_id_seq RESTART 50000;

