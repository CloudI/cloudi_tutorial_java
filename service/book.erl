%% @author Bruce Kissinger
%% @version 1.1.0

% @doc This module provides a common service for the Book Recommendation system.
%It leverages the CloudI framework to provide a cloud-based service that can be used by many different programming languages.
%
%The exported functions are required interfaces that the service must implement.
 
-module(book).
-behaviour(cloudi_service).

%% cloudi_service callbacks
-export([cloudi_service_init/4,
         cloudi_service_handle_request/11,
         cloudi_service_handle_info/3,
         cloudi_service_terminate/3]).

-include_lib("cloudi_core/include/cloudi_logger.hrl").

% Optional include needed to run PropEr test suite
%-include_lib("proper/include/proper.hrl").

%% record definition
-record(state,
    {
    }).

-record(item,
    {
	id,
	title,
	creator,
	language,
	date_created,
	web_page,
	subject,
	downloads	
    }).

%% Define the name of the CloudI database service 
-define(NAME_MYSQL, "/db/mysql/book").

%% Define common types
-type any_type() :: any().
-type dispatcher_type() :: pid().
-type item_id_type() :: nonempty_string().
-type name_type() :: nonempty_string().
-type ok_type() :: 'ok'.
-type ok_state_type() :: {'ok', #state{}}.
-type pattern_type() :: nonempty_string().
-type rated_flag_type() :: nonempty_string().
-type rating_type() :: integer().
-type record_type() :: any().
-type request_type() :: nonempty_string().
-type state_type() :: any().		%% enhance this
-type type_type() :: nonempty_string().	
-type user_id_type() :: nonempty_string().

%%%------------------------------------------------------------------------
%%% Exported functions
%%%------------------------------------------------------------------------

%% @doc Initialize the internal service
%%-spec cloudi_service_init(_Args, _Prefix, _Timeout, dispatcher_type()) -> ok_state_type.
-spec cloudi_service_init(_, _, _, dispatcher_type()) -> ok_state_type().
cloudi_service_init(_Args, _Prefix, _Timeout, Dispatcher) ->

	% subscribe to different request patterns
	cloudi_service:subscribe(Dispatcher, "newbooks/get"),
	cloudi_service:subscribe(Dispatcher, "popularbooks/get"),
	cloudi_service:subscribe(Dispatcher, "recommendedbooks/get"),
	cloudi_service:subscribe(Dispatcher, "allbooks/get"),
	cloudi_service:subscribe(Dispatcher, "download/get"),
	cloudi_service:subscribe(Dispatcher, "newuser/get"),
	cloudi_service:subscribe(Dispatcher, "unrated/get"),
	cloudi_service:subscribe(Dispatcher, "rate/get"),
	cloudi_service:subscribe(Dispatcher, "/post"),

    	{ok, #state{}}.

%% @doc Handle an incoming service request 
-spec cloudi_service_handle_request(type_type(), name_type(), pattern_type(), _, request_type(), _, _, _, _, state_type(), dispatcher_type()) -> record_type().
cloudi_service_handle_request(Type, Name, Pattern, _RequestInfo, Request,
                              _Timeout, _Priority, _TransId, _Pid,
                              #state{} = State, Dispatcher) ->
    
    	?LOG_INFO("Handle Request: Type=~p, Name=~p, Pattern=~p, Request=~p", [Type, Name, Pattern, Request]),

	% based on the pattern and request, perform the appropriate action
	case Pattern of
		"/recommend/book/newbooks/get" ->
			ReplyRecord = find_new(Dispatcher);
		"/recommend/book/popularbooks/get" ->
			ReplyRecord = find_popular(Dispatcher);
		"/recommend/book/recommendedbooks/get" ->
			% parse the user id
			List = binary_to_list(Request),
			Request_tokens = string:tokens(List, "="),
			?LOG_DEBUG("Request_tokens=~p", [Request_tokens]),

			case Request_tokens of 
				["user", Id] ->
			                ReplyRecord = get_recommended_items(Id, Dispatcher);
				_ ->
					ReplyRecord = cloudi_x_jsx:encode(["Could not match request"])
			end;

		"/recommend/book/allbooks/get" ->
			ReplyRecord = find_item(Request, Dispatcher);
		"/recommend/book/download/get" ->
			%TODO: change the item_id and user_id to be passed correctly per spec
			% parse the item id and user id
			List = binary_to_list(Request),
			Request_tokens = string:tokens(List, ","),
			?LOG_DEBUG("Request_tokens=~p", [Request_tokens]),
			
			[Item_id, User_id] = Request_tokens,
			ReplyRecord = add_user_item(User_id, Item_id, Dispatcher);

		"/recommend/book/newuser/get" ->
			ReplyRecord = add_user(Dispatcher);

		"/recommend/book/unrated/get" ->
			List = binary_to_list(Request),
			Request_tokens = string:tokens(List, "="),
			?LOG_DEBUG("Request_tokens=~p", [Request_tokens]),

			case Request_tokens of 
				["user", User_id] ->
					ReplyRecord = find_unrated_items(User_id, Dispatcher);
				_ ->
					ReplyRecord = cloudi_x_jsx:encode(["Could not match request"])
			end;

		"/recommend/book/rate/get" ->
			% parse the item id, user id, and rating
			List = binary_to_list(Request),
			Request_tokens = string:tokens(List, "&"),
			?LOG_DEBUG("Request_tokens=~p", [Request_tokens]),
		
			[Id_list, User_list, Rating_list] = Request_tokens,
			
			Id_tokens = string:tokens(Id_list, "="),
			["id", Item_id] = Id_tokens,

			User_tokens = string:tokens(User_list, "="),
			["user", User_id] = User_tokens,
			
			Rating_tokens = string:tokens(Rating_list, "="),
			["rating", Rating] = Rating_tokens,
			{Rating_as_integer, _} = string:to_integer(Rating),	

			ReplyRecord = add_rating(Item_id, User_id, Rating_as_integer, Dispatcher);
		"/recommend/book//post" ->
			List = binary_to_list(Request),
			Request_tokens = string:tokens(List, "="),
			?LOG_DEBUG("Request_tokens=~p", [Request_tokens]),

			case Request_tokens of 
				["item", Id] ->
					ReplyRecord = find_item(Id, Dispatcher);
				_ ->
					ReplyRecord = cloudi_x_jsx:encode(["Could not match request"])
			end;

			
%		"/recommend/book/utility/totaldownloads/get" ->
%			Answer = get_total_downloads(Dispatcher),
%			ReplyRecord = list_to_binary(integer_to_list(Answer));

		_ ->
			ReplyRecord = cloudi_x_jsx:encode(["Invalid Request"])
	end,

	% send reply
        ?LOG_DEBUG("Sending reply=~p", [ReplyRecord]),
	{reply, ReplyRecord, State}.
 

%% @doc Handle an incoming Erlang message
cloudi_service_handle_info(Request, State, _) ->
    ?LOG_INFO("Message received\"~p\"", [Request]),
    {noreply, State}.

-spec cloudi_service_terminate(_Reason, _Timeout, _) -> ok.
%% @doc Terminate the service
cloudi_service_terminate(_Reason, _Timeout, #state{}) -> ok.  

%%%------------------------------------------------------------------------
%%% Service functions
%%%------------------------------------------------------------------------

%% @TODO:  Add type descriptions for the function parameters
%% @TODO:  Fix guard functions on input parameters

-spec find_item(item_id_type(), dispatcher_type()) -> record_type().
%find_item(Id, Dispatcher) when is_integer(Id) ->
find_item(Id, Dispatcher) ->
	?LOG_DEBUG("find_item Id =~p", [Id]),

	Query = string:concat("select id, title, creator, lang, date_created, web_page, subject, download_quantity from items where id=", Id), 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , Result} ->
			?LOG_DEBUG("Result =~p", [Result]),
			Json_result = parse_items(Result);
		_ ->
			?LOG_DEBUG("No data found", []),
			Json_result = cloudi_x_jsx:encode(<<"No data found">>)
	end,
			
	Json_result.	


-spec find_new(dispatcher_type()) -> record_type().
find_new(Dispatcher) ->

	Query = "select  id, title from items where date_created > date_sub(curdate(),interval 30 day) order by date_created desc", 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , Result} ->
			?LOG_DEBUG("Result =~p", [Result]),
			Json_result = parse_items(Result);
		_ ->
			?LOG_DEBUG("No data found", []),
			Json_result = cloudi_x_jsx:encode([<<"no data found">>])
	end,
			
	Json_result.	


-spec find_popular(dispatcher_type()) -> record_type().
find_popular(Dispatcher) ->

	Query = "select id, title from items order by download_quantity desc limit 300", 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , Result} ->
			?LOG_DEBUG("Result =~p", [Result]),
			Json_result = parse_items(Result);
		_ ->
			?LOG_DEBUG("No data found", []),
			Json_result = cloudi_x_jsx:encode(<<"No data found">>)
	end,
			
	Json_result.	


%-spec add_rating(item_id_type(), user_id_type(), rating_type(), dispatcher_type()) -> ok_type().
add_rating(Item_id, User_id, Rating, Dispatcher) ->
	?LOG_DEBUG("add_rating User_id=~p, Item_id=~p, Rating=~p", [User_id, Item_id, Rating]),

	Query = lists:concat(["INSERT INTO user_item_ratings (user_id, item_id, rating) values (", User_id,  ",", Item_id, ",",  Rating, ")"]),
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , {ok_packet, _, _, _, _, _, _}} ->
			update_user_item(User_id, Item_id, "Y", Dispatcher),
			Json_result = cloudi_x_jsx:encode(<<"ok">>);
		_ ->
			?LOG_DEBUG("Error Processing Request ~p", [Status]),
			Json_result = cloudi_x_jsx:encode(<<"Error processing request">>)
	end,
			
	Json_result.	

-spec get_recommended_items(user_id_type(), dispatcher_type()) -> record_type().
get_recommended_items(User_id, Dispatcher) ->
	?LOG_DEBUG("get_recommended_items =~p", [User_id]),

	Query = string:concat("select id, title from items left join user_item_recommendations on items.id = user_item_recommendations.item_id where user_item_recommendations.user_id=", User_id), 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , Result} ->
			?LOG_DEBUG("Result =~p", [Result]),
			Json_result = parse_items(Result);
		_ ->
			?LOG_DEBUG("No data found", []),
			Json_result = cloudi_x_jsx:encode([<<"no data found">>])
	end,
			
	Json_result.	

-spec find_unrated_items(user_id_type(), dispatcher_type()) -> record_type().
find_unrated_items(User_id, Dispatcher) ->
	?LOG_DEBUG("find_unrated_items =~p", [User_id]),

	Query = string:concat("select id, title from items inner join user_items on items.id = user_items.item_id where user_items.rated_flag='N' and user_items.user_id=", User_id), 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , Result} ->
			?LOG_DEBUG("Result =~p", [Result]),
			Json_result = parse_items(Result);
		_ ->
			?LOG_DEBUG("No data found", []),
			Json_result = cloudi_x_jsx:encode([<<"no data found">>])
	end,

	Json_result.	
			

-spec add_user_item(user_id_type(), item_id_type(), dispatcher_type()) -> record_type().
add_user_item(User_id, Item_id, Dispatcher) ->
	?LOG_DEBUG("add_user_item User_id=~p, Item_id=~p", [User_id, Item_id]),

	Query= lists:concat(["INSERT INTO user_items (user_id, item_id, rated_flag) values (",  User_id, ",", Item_id, ",'N')"]), 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , {ok_packet, _, _, _, _, _, _}} ->
			Json_result = cloudi_x_jsx:encode(<<"ok">>);
		_ ->
			?LOG_DEBUG("Error Processing Request ~p", [Status]),
			Json_result = cloudi_x_jsx:encode(<<"Error processing request">>)
	end,
			
	Json_result.	

-spec update_user_item(user_id_type(), item_id_type(), rated_flag_type(), dispatcher_type()) -> any().
update_user_item(User_id, Item_id, Rated_flag, Dispatcher) ->
	?LOG_DEBUG("update_user_item User_id=~p, Item_id=~p, Rated flag=~p", [User_id, Item_id, Rated_flag]),

	Query= lists:concat(["UPDATE user_items set rated_flag='", Rated_flag, "' where user_id=", User_id, " and item_id=", Item_id]), 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , {ok_packet, _, _, _, _, _, _}} ->
			Json_result = cloudi_x_jsx:encode(<<"ok">>);
		_ ->
			?LOG_DEBUG("Error Processing Request ~p", [Status]),
			Json_result = cloudi_x_jsx:encode(<<"Error processing request">>)
	end,
			
	Json_result.	

-spec add_user(dispatcher_type()) -> record_type().
add_user(Dispatcher) ->
	?LOG_DEBUG("add_user", []),

	Query= lists:concat(["INSERT INTO users (user_id, date_created) values (null, now());"]), 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , {ok_packet, _, _, _, _, _, _}} ->

			User_id = get_last_insert_id(Dispatcher),
			Json_result = cloudi_x_jsx:encode(User_id);
		_ ->
			?LOG_DEBUG("Error Processing Request ~p", [Status]),
			Json_result = cloudi_x_jsx:encode(<<"Error processing request">>)
	end,

	Json_result.	


-spec get_last_insert_id(dispatcher_type()) -> record_type().
get_last_insert_id(Dispatcher) ->
	?LOG_DEBUG("get_last_insert_id", []),

	Query= lists:concat(["SELECT last_insert_id();"]), 
	?LOG_DEBUG("query=~p", [Query]),

	Status = cloudi_service:send_sync(Dispatcher, 
		?NAME_MYSQL, 
		<<>>, 
		Query, 
		undefined, 
		undefined), 
	
	case Status of 
		{ok , Result} ->
			?LOG_DEBUG("Result =~p", [Result]),
			User_id = parse_insert_id(Result);
		_ ->
			?LOG_DEBUG("No data found", []),
			User_id = 0
	end,
			
	User_id.	


parse_items({result_packet, _, Columns, List, _Trailer}) ->
	?LOG_DEBUG("Columns=~p", [Columns]),
	?LOG_DEBUG("List=~p", [List]),

	Return_value = parse_item(List),
	Return_value.

parse_item(List)  ->
	parse_item(List, []).

parse_item([H|T], Return_value)  ->
	% Note that the record can contain different numbers of colulmns
	% and that the columns need to be in the correct positions
	case H of 
		[Id, Title, Author, Language, Date, Web_page, Subject, Downloads] -> 
			Item = #item{id=Id, title=Title, creator=Author, language=Language, date_created = Date, web_page=Web_page, subject=Subject, downloads=Downloads},	

			Encoded_item = cloudi_x_jsx:encode(
				[
	  			  {<<"id">>, Item#item.id},
				  {<<"title">>, Item#item.title},
				  {<<"creator">>, Item#item.creator},
				  {<<"language">>, Item#item.language},
	%%			  {<<"date_created">>, Item#item.date_created},
				  {<<"web_page">>, Item#item.web_page},
				  {<<"subject">>, Item#item.subject},
				  {<<"downloads">>, Item#item.downloads}
				]);

		[Id, Title] ->
			Item = #item{id=Id, title=Title},

			Encoded_item = cloudi_x_jsx:encode(
				[
 				  {<<"id">>, Item#item.id},
				  {<<"title">>, Item#item.title}
				]) 
	
	end,	

	?LOG_TRACE("Item=~p", [Item]),

	Temp_return_value = [Return_value | Encoded_item], 
	New_return_value = [Temp_return_value | ","], 

	parse_item(T, New_return_value);

parse_item([], Return_value) ->
	% strip the trailing comma from the return value
	Temp = string:strip(Return_value, right, $,),

	% add brackets around the return string
	string:concat(string:concat("[", Temp), "]").


parse_insert_id({result_packet, _, _Columns, List, _Trailer}) ->
	%?LOG_DEBUG("Columns=~p", [Columns]),
	%?LOG_DEBUG("List=~p", [List]),

	[[Return_value]] = List,
	?LOG_DEBUG("Return value =~p", [Return_value]),
	Return_value.

