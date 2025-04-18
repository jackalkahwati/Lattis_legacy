""" a Region is a deployment zone for a client. it belongs to a client ( owner) and contains a geojson
describing boundaries, and is linked to a list of geohashes that have data for the algorithm to run """

from sqlalchemy import Column, ForeignKey, Integer, String, JSON
from sqlalchemy.orm import relationship
import json
from lattis_ds import utils
from .database import Base


class Region(Base):
    __tablename__ = "region"

    id = Column(String, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    geojson = Column(JSON, default='{}')
    parameters = Column(JSON, default='{}')
    ghs_data = Column(JSON, default='{}')

    owner_id = Column(Integer, ForeignKey("users.id"))
    owner = relationship("User", back_populates="regions")

    def get_geohashes_from_geojson_polygon(self, precision=7, inner=True):
        """ return a list of geohash names for a polygon """
        json_dict = json.loads(self.geojson)
        return utils.convert_polygon_to_list_of_geohashes(json_dict, precision=precision, inner=inner)
