def dump_vector(x):
    return ",".join(map(str, x))

def dump_matrix(xs):
    vs = map(dump_vector, xs)
    return "{0}#{1}#{2}".format(xs.shape[0], xs.shape[1], "|".join(vs))

def dump_pca(pca):
    return "{0}\n{1}".format(dump_matrix(pca.components_.T), dump_vector(pca.mean_))

def dump_mlp(mlp):
    activation = mlp.activation
    layers = []
    for c, b in zip(mlp.coefs_, mlp.intercepts_):
        layers.append(dump_matrix(c.T))
        layers.append(dump_vector(b))
    return "{0}\n{1}\n{2}".format(activation, int(len(layers) / 2), "\n".join(layers))

def save_pca(pca, path):
    with open(path, "w") as f:
        f.write(dump_pca(pca))
        f.close()

def save_mlp(mlp, path):
    with open(path, "w") as f:
        f.write(dump_mlp(mlp))
        f.close()
