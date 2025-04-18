
class NonexistentSchemaException(Exception):
    def __init__(self, schema):
        message = "Schema {schema} doesn't exist".format(schema=schema)
        Exception.__init__(self, message)


class NonexistentTableException(Exception):
    def __init__(self, schema, table):
        message = "Table {table} doesn't exist in schema {schema}".format(schema=schema, table=table)
        Exception.__init__(self, message)


class NonexistentTriggerException(Exception):
    def __init__(self, schema, trigger):
        message = "Trigger {trigger} doesn't exist in schema {schema}".format(schema=schema, trigger=trigger)
        Exception.__init__(self, message)


class NoDataException(Exception):
    def __init__(self, schema, table):
        message = "Could not find data for table {table} in schema {schema}".format(schema=schema, table=table)
        Exception.__init__(self, message)