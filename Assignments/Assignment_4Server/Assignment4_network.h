#define TYPE_DISCOVER 1
#define TYPE_PLUG_LIST 2
#define TYPE_SWITCH_ON 3
#define TYPE_SWITCH_OFF 4
#define TYPE_STATUS_CHANGED 5
#define TYPE_STATUS_ON 6
#define TYPE_STATUS_OFF 7

typedef struct {
	uint8_t type;
} __attribute__ ((__packed__)) message_type_struct;

typedef struct {
	uint8_t id;
} __attribute__ ((__packed__)) plugId_struct;

typedef struct {
	uint8_t status;
} __attribute__ ((__packed__)) plug_status_struct;

typedef struct {
	message_type_struct message_type;
} __attribute__ ((__packed__)) discover_msg;

typedef struct {
	plugId_struct plugId;
	plug_status_struct status;
	uint8_t name_length;
	char * name;
} __attribute__ ((__packed__)) plug_info;

typedef struct {
	message_type_struct message_type;
	uint8_t plugs_number;
	plug_info * plugs;
} __attribute__ ((__packed__)) plug_list_msg;

typedef struct {
	message_type_struct message_type;
	plugId_struct plugId;
} __attribute__ ((__packed__)) action_msg;

typedef struct {
	message_type_struct message_type;
	plugId_struct plugId;
	plug_status_struct status;
} __attribute__ ((__packed__)) status_changed_msg;
