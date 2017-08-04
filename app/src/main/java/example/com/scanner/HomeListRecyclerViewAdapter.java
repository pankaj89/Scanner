package example.com.scanner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import example.com.scanner.database.Attachment;
import example.com.scanner.databinding.ItemHomeListBinding;

/**
 * Created by Pankaj Sharma on 19/6/17.
 */

public class HomeListRecyclerViewAdapter extends RecyclerView.Adapter<HomeListRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Attachment> list;

    public HomeListRecyclerViewAdapter(ArrayList<Attachment> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemHomeListBinding itemBinding = ItemHomeListBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public int addItem(Attachment attachment) {
        list.add(0, attachment);
        notifyDataSetChanged();
        return 0;
    }

    public void updateItem(int position, Attachment attachment) {
        list.set(position, attachment);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemHomeListBinding itemBinding;

        public ViewHolder(ItemHomeListBinding binding) {
            super(binding.getRoot());
            itemBinding = binding;
        }

        public void bind() {
            itemBinding.setAttachment(getItem(getAdapterPosition()));
            itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
            itemBinding.imgCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) itemBinding.getRoot().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("copy", getItem(getAdapterPosition()).path);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(itemBinding.getRoot().getContext(), "Copied", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public Attachment getItem(int position) {
        return list.get(position);
    }

    public interface OnItemClickListener {
        public void onItemClick(int position);
    }

    OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
