# CrossLink API documentation

API Version: `1.0.0`

Compatible CrossLink version: `1.0-SNAPSHOT`

## `/online_players`

- Method: `GET`
- GET Parameters:
  - `grouped` (**int**, **optional**) If set to `1`, returns online players grouped by the servers they currently on.
    Otherwise, returns all online players in one list.
- Returns: A non-null JSON dictionary, containing a non-null value `players`.
           If `grouped=1`, `players` is a non-null dictionary. Otherwise, `players` is a non-null list.
- Example:
  - `GET /online_players`: `{"players":[]}`
  - `GET /online_players`: `{"players":["fakeKeuin","trueKeuin"]}`
  - `GET /online_players?grouped=1`: `{"players":[]}`
  - `GET /online_players?grouped=1`: `{"players":{"server2":["fakeKeuin"],"server1":["trueKeuin"]}}`
  - `GET /online_players?grouped=1`: `{"players":{"server2":["fakeKeuin", "trueKeuin"]}}`


## `/server_status`

- Method: `GET`
- GET Parameters: none
- Returns: A non-null JSON dictionary, containing `ServerName`(`string`) -> `ServerStatus`(`dictionary`) mapping.
           A `ServerStatus` object contains `status` key, which is an **string** enum `Status`.
- Range of enum `Status`: `UP`, `DOWN`, `TIMED OUT` 
- Example:
  - `GET /server_status`: `{"servers":{"server1":{"status":"UP"},"server2":{"status":"UP"}}}`