from game import State
import pickle
import os

def first_player_value(ended_state):
    # 1: 선 수 플레이어 승리, -1: 선 수 플레이어 패배, 0: 무승부
    if ended_state.is_lose():
        return -1 if ended_state.is_first_player() else 1
    if ended_state.is_win():
        return 1 if ended_state.is_first_player() else -1
    return 0

os.makedirs('./data/preprocessed_eg_data', exist_ok=True)  # 폴더가 없는 경우에는 생성
ind = 1
all_history = []
with open('./data/egaroucid_data/0000000.txt') as f:
    for line in f:
        history = []
        state = State()
        for j in range(len(line)//2):
            if state.legal_actions()[0]==64:
                state = state.next(64)
            a = ord(line[j*2]) - ord('a')
            b = int(line[j*2+1])
            mov = (b-1)*8 + a
            policies = [0] * 65
            policies[mov] = 1
            history.append([[state.pieces, state.enemy_pieces], policies, None])
            state = state.next(mov)
        value = first_player_value(state)
        for i in range(len(history)):
            history[i][2] = value
            value = -value
        all_history.extend(history)
        print(" Game #" + str(ind) + " successfull")
        ind+=1
path = './data/preprocessed_eg_data/0000000.history'
with open(path, mode='wb') as h:
    pickle.dump(all_history, h)