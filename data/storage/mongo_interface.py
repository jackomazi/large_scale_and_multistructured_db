from bson.objectid import ObjectId

class mongo_db_interface:
    @staticmethod
    # Storing user to MongoDB
    def store_dict_to_MongoDB(document: dict, collection) -> ObjectId:
        # To be extra sure
        if "_id" in document:
            document.pop("_id")
        
        result = collection.insert_one(document)
        return result.inserted_id