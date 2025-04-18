from setuptools import setup, find_packages


long_description = '''
Python package and API for lattis to deal with data science related problems such as dynamic pricing
and repositioning of vehicles'''

setup(
    name='lattis_ds',
    version='0.0.0.1',
    description='Python Utility Package Data team at Latts',
    long_description=long_description,
    author='clement@unsupervised.ai',
    packages=find_packages(),
    install_requires=[
        'psycopg2==2.8.4'  # TODO add more dependencies here
    ]
)
