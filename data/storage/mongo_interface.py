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
    
    @staticmethod
    def add_members_to_team_in_MongoDB(team_mongo_id: ObjectId, members: list, collection):
        collection.update_one(
            {"_id": team_mongo_id},
            {"$push": {"members": {"$each": members}}}
        )

    @staticmethod
    def add_games_to_tournament_in_MongoDB(tournament_mongo_id: ObjectId, games: list, collection):
        collection.update_one(
            {"_id": tournament_mongo_id},
            {"$push": {"games": {"$each": games}}}
        )
        
    @staticmethod
    def add_games_to_user_in_MongoDB(user_mongo_id: ObjectId, games: list, collection):
        collection.update_one(
            {"_id": user_mongo_id},
            {"$push": {"games": {"$each": games}}}
        )

    
