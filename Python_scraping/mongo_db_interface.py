
class mongo_db_interface:

    @staticmethod
    # Storing user to MongoDB
    def store_dict_to_MongoDB(user: dict, collection):
        collection.replace_one(
            {"_id": user["_id"]}, 
            user,                 
            upsert=True
        )