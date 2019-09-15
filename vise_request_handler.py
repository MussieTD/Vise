# -*- coding: utf-8 -*-
"""
Created on Sun Sep 15 10:48:32 2019

@author: muste
"""

import math
import random

# Add all these things within the request handler.
def request_handler(request):
    if request['method'] == "GET":
        
        beacons = request['args']
        #print(beacons)
        item = str(request['values']['item']) #TODO: look up item
        mapping = {}
        for beacon in beacons:
            if beacon == 'item':
                continue
            mapping[beacon] = float(request['values'][beacon])
        
        #print("mapping: ", mapping)
        
        board = make_board((5,7),0)
        di,ID = get_mapping(board)
        aisle_beacons = {"HackA":(1,1), "HackB":(1,2), "HackC":(1,4), "HackD":(1,5), "HackE":(3,1), "HackF":(3,2), "HackG":(3,4), "HackH":(3,5)}
        location = get_location(aisle_beacons)
        
        choose = [i for i in range(len(di))]
        chosen = random.choice(choose)
        
        x,y = ID[chosen]
        
        board[x][y] = 2
        
        person_location = (0,1)

        board[person_location[0]][person_location[1]] = 3
        
        path = guide_person(board, location)
        
        lst_path = commands(path)
        
        str_path = ""
        
        for i in range(len(lst_path)-1):
            str_path += str(lst_path[i]) + "\n"
        
        if(len(path)>1):
            str_path += lst_path[-1] + "\n"
        
        return str_path
    
    
def get_location(aisle_beacons):
    
    return (0,3)


def create_aisle(board):
    ofset = 2
    space = 3
    apr = 2
    l = 5
    for i in range(ofset, len(board)):
        if i%space == 0:
            index = 0
            for count in range(apr):
                drawn = 0
                while drawn < l:
                    board[i][ofset+index] = 1
                    index+=1
                    drawn+=1
                index += space



def make_board(dimensions, elem):
    if len(dimensions) == 1:
        return [elem]*dimensions[0]

    else:
        k = [make_board(dimensions[1:], elem) for i in range(dimensions[0])]
        return k

def get_mapping(board):
    index = 0
    di = {}
    ID = {}
    m = len(board)
    n = len(board[0])
    for i in range(m):
        for j in range(n):
            di[(i,j)] = index
            ID[index] = (i,j)
            index += 1
    return (di,ID)

def get_n(point, m, n):
    points = [(point[0]+1,point[1]),(point[0]-1, point[1]),(point[0], point[1]+1),(point[0], point[1]-1)]
    good = []
    for p in points:
        if p[0] < 0 or p[1] < 0:
            continue
        elif p[0] >= m or p[1] >= n:
            continue
        else:
            good.append(p)
    return good

def create_graph(board):
    Adj = []
    m = len(board)
    n = len(board[0])
    di = {}
    di,ID = get_mapping(board)
    for i in range(len(board)):
        for j in range(len(board[0])):
            neigh = get_n((i,j), m, n)
            edges = []
            for ne in neigh:
                if board[ne[0]][ne[1]] == 1:
                    continue
                edges.append(di[ne])
            Adj.append(edges)
    return Adj

def bfs(Adj, s):
    parent = [None for v in Adj]
    parent[s] = s
    level = [[s]]
    while 0 < len(level[-1]):
        level.append([])
        for u in level[-2]:
            for v in Adj[u]:
                if parent[v] is None:
                    parent[v] = u
                    level[-1].append(v)
    return parent

def unweighted_shortest_path(Adj, s, t):
    parent = bfs(Adj, s) 
    if parent[t] is None:
        return None
    i = t 
    path = [t] 
    while i != s: 
        i = parent[i]
        path.append(i)
    return path[::-1]

def get_distance(p1, p2):
    return math.sqrt((p2[1]-p1[1])**2 + (p2[0]-p2[1])**2)

def get_closest_item(p, all_points):
    min_dist = float('inf')
    min_point = None
    for point in all_points:
        d = get_distance(p, point)
        if d < min_dist:
            min_dist = d
            min_point = point
    return min_point

def get_store_items(board):
    items = set()
    m = len(board)
    n = len(board[0])
    for i in range(m):
        for j in range(n):
            if board[i][j] == 2:
                items.add((i,j))
    return items

def guide_person(board, starting_point):
    items_to_get = get_store_items(board)
    s = starting_point
    Adj = create_graph(board)
    di,ID = get_mapping(board)
    final_directions = []
    while(len(items_to_get) > 0):
        t = get_closest_item(s, items_to_get)
        directions = unweighted_shortest_path(Adj, di[s], di[t])
        s = ID[directions[-1]]
        xr,yr = ID[directions[-1]] 
        items_to_get.remove((xr,yr))
        soln = [ID[n] for n in directions]
        final_directions.append(soln)
        #print(final_directions)
        for point in directions:
            pt = ID[point]
            board[pt[0]][pt[1]] = 3
    if len(final_directions) > 0:
        return final_directions[0]
    else:
        return []

def commands(path):
    if len(path) != 0:
    	commands = []
    	prev = path[0]
    	for i in range(len(path)-1):
    		commands.append(decide(prev, path[i], path[i+1]))
    		prev = path[i]
    	return commands
    else: 
        return ""

def decide(p1, p2, p3):
	if (p1[0]==p2[0] and p2[0]==p3[0]) or (p1[1]==p2[1] and p2[1]==p3[1]) :
		return "move straight"
	if p1[1]==p2[1]:
		if (p3[1]>p2[1] and p1[0]<p2[0]) or (p3[1]<p2[1] and p1[0]>p2[0]):
			return "turn left"
		else:
			return "turn right"
	if p1[0] == p2[0]:
		if (p3[0]<p2[0] and p1[1]<p2[1]) or (p3[0]>p2[0] and p1[1]>p2[1]):
			return "turn left"
		else:
			return "turn right"