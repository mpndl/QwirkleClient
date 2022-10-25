package za.nmu.wrpv.qwirkle;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

public class PlayerTilesAdapter extends RecyclerView.Adapter<PlayerTilesAdapter.ImageViewHolder> {
    public List<Tile> tiles;
    private final Activity context;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    public PlayerTilesAdapter(Activity context, List<Tile> tiles) {
        this.tiles = tiles;
        this.context = context;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    private int getDrawableInt(String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    public void updateTiles(List<Tile> playerTiles) {
        this.tiles = playerTiles;
        notifyDataSetChanged();
    }

    public void add(Tile tile) {
        tiles.add(tile);
        notifyItemInserted(getItemCount() - 1);
    }

    public void addAll(List<Tile> tiles) {
        this.tiles.addAll(tiles);
        notifyDataSetChanged();
    }

    public Tile get(int index) {
        return tiles.get(index);
    }

    public void remove(Tile tile) {
        int index = tiles.indexOf(tile);
        tiles.remove(index);
        notifyItemRemoved(index);
    }

    public void removeAll(List<Tile> tiles){
        this.tiles.removeAll(tiles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.board_tile,parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Tile tile = tiles.get(position);
        holder.setImageResource(tile, position);
        holder.itemView.setOnClickListener(onClickListener);
        holder.itemView.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }

    protected class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_tile);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = 50;
            imageView.setLayoutParams(params);
        }

        public void setImageResource(Tile tile, int position) {
            int drawableInt = getDrawableInt(tile.toString());
            imageView.setImageResource(drawableInt);
            imageView.setTag(position);
        }
    }
}
