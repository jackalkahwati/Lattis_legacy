class Model:
    def _get_property(self, property, model_dicionary):
        return model_dicionary[property] if property in model_dicionary else None
