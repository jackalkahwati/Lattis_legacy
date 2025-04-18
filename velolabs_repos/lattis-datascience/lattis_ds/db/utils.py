import re


def get_query_string(filename_or_string):
    """
    parser to read sql statements from files/strings

    >>> get_query_string('create temporary table stuff; select * from stuff')
    ['create temporary table stuff; select * from stuff']
    """
    if filename_or_string.endswith('.sql'):
        with open(filename_or_string, 'r') as f:
            query_string = f.read()
    else:
        query_string = str(filename_or_string)
    # return map(lambda q: q.strip(), query_string.split(';'))
    return [query_string]


def has_sql_content(sql):
    """
    does the string have anything that isn't a blank line or a sql comment

    >>> has_sql_content('')
    False
    >>> has_sql_content("-- some comments here\\n\\n")
    False
    >>> has_sql_content("-- select * from commented_out_query")
    False
    >>> has_sql_content("select * from table -- some comments here\\n\\n")
    True
    >>> has_sql_content("-- some comments here\\n\\nselect * from table")
    True
    """
    return any(len(ln.strip()) > 0 for ln in sql.splitlines() if not ln.strip().startswith('--'))


def clean_sql(sql):
    """
    clean up sql patterns that will break interpreter.
    * query can't have single % as these are interpreted by python formatter
      so convert any "%" (and not like %(stuff)s) to escaped "%%"
    * also, remove any trailing semicolons

    >>> clean_sql('select pct from table -- percent is like 100.0%')
    'select pct from table -- percent is like 100.0%%'
    >>> clean_sql('select pct + %(var)s from table')
    'select pct + %(var)s from table'
    >>> clean_sql('select pct from table;')
    'select pct from table'
    >>> clean_sql('select pct from table; select * from other;')
    'select pct from table; select * from other'
    """
    return re.sub('(?<!%)%(?![%(])', '%%', sql).strip().rstrip(';').strip()


def is_table_transaction(sql):
    """
    is this a create/alter/insert/update/drop table statement?

    >>> is_table_transaction('SELECT * FROM stuff')
    False
    >>> is_table_transaction('SET search_path to event')
    True
    >>> is_table_transaction('CREATE TABLE new_table AS SELECT * FROM other;')
    True
    """
    return re.search(r"\b(CREATE|ALTER|UPDATE|INSERT|DROP|SET|GRANT|DELETE)\b", sql, re.IGNORECASE) is not None
